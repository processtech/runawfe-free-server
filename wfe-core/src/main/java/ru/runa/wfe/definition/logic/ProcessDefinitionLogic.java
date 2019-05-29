package ru.runa.wfe.definition.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentAdminActionLog;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CheckMassPermissionCallback;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.ProcessDefinitionInfoVisitor;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.presentation.hibernate.RestrictionsToOwners;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

@Component
public class ProcessDefinitionLogic extends WfCommonLogic {

    /**
     * @param secondsBeforeArchiving
     *              If null or negative, will be nulled in database (default will be used).
     */
    public WfDefinition deployProcessDefinition(User user, byte[] processArchiveBytes, List<String> categories, Integer secondsBeforeArchiving) {
        permissionDao.checkAllowed(user, Permission.CREATE, SecuredSingleton.DEFINITIONS);
        ParsedProcessDefinition parsed;
        try {
            parsed = parseProcessDefinition(processArchiveBytes);
        } catch (Exception e) {
            throw new DefinitionArchiveFormatException(e);
        }
        try {
            getLatestDefinition(parsed.getName());
            throw new DefinitionAlreadyExistException(parsed.getName());
        } catch (DefinitionDoesNotExistException e) {
            // expected
        }
        if (secondsBeforeArchiving != null && secondsBeforeArchiving < 0) {
            secondsBeforeArchiving = null;
        }
        ProcessDefinition d = parsed.getProcessDefinition();
        ProcessDefinitionVersion dv = parsed.getProcessDefinitionVersion();
        d.setCategories(categories);
        d.setSecondsBeforeArchiving(secondsBeforeArchiving);
        dv.setCreateDate(new Date());
        dv.setCreateActor(user.getActor());
        dv.setVersion(1L);
        dv.setSubVersion(0L);
        processDefinitionDao.create(d);
        processDefinitionVersionDao.create(dv);
        d.setLatestVersion(dv);
        permissionDao.setPermissions(user.getActor(), Collections.singletonList(Permission.ALL), d);
        log.debug("Deployed process definition " + parsed);
        return new WfDefinition(parsed, permissionDao.isAllowed(user, Permission.START, d));
    }

    /**
     * Redeploys process definition by name, by creating new definition version.
     *
     * @param secondsBeforeArchiving
     *              If null, old value will be used (compatibility mode); if negative, will be nulled in database (default will be used).
     */
    public WfDefinition redeployProcessDefinition(User user, long processDefinitionVersionId, byte[] processArchiveBytes, List<String> categories,
            Integer secondsBeforeArchiving) {
        ProcessDefinitionWithVersion dwvOld = processDefinitionDao.findDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, dwvOld.processDefinition);
        if (processArchiveBytes == null) {
            Preconditions.checkNotNull(categories, "In mode 'update only categories' categories are required");
            dwvOld.processDefinition.setCategories(categories);
            if (secondsBeforeArchiving != null) {
                dwvOld.processDefinition.setSecondsBeforeArchiving(secondsBeforeArchiving < 0 ? null : secondsBeforeArchiving);
            }
            return getProcessDefinition(user, processDefinitionVersionId);
        }

        if (!Objects.equals(dwvOld.processDefinitionVersion.getId(), dwvOld.processDefinition.getLatestVersion().getId())) {
            throw new InternalApplicationException("Latest version of process definition '" + dwvOld.processDefinition.getName() + "' is '" +
                    dwvOld.processDefinition.getLatestVersion().getVersion() + "'. You provided processDefinitionVersionId for version '" +
                    dwvOld.processDefinitionVersion.getVersion() + "'");
        }

        ParsedProcessDefinition parsed = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(dwvOld.processDefinition.getName(), parsed.getName())) {
            throw new DefinitionNameMismatchException(dwvOld.processDefinition.getName(), parsed.getName());
        }
        try {
            checkCommentsOnDeploy(parseProcessDefinition(dwvOld.processDefinitionVersion.getContent()), parsed);
        } catch (InvalidDefinitionException e) {
            log.warn(dwvOld + ": " + e);
        }

        // We don't create new ProcessDefinition record here, but copy data from parsed to old...
        ProcessDefinition d = parsed.getProcessDefinition();
        ProcessDefinitionVersion dv = parsed.getProcessDefinitionVersion();
        dwvOld.processDefinition.setDescription(d.getDescription());
        dwvOld.processDefinition.setLanguage(d.getLanguage());
        if (categories != null) {
            dwvOld.processDefinition.setCategories(categories);
        }
        if (secondsBeforeArchiving != null) {
            dwvOld.processDefinition.setSecondsBeforeArchiving(secondsBeforeArchiving < 0 ? null : secondsBeforeArchiving);
        }
        // ...and continue working with old.
        d = dwvOld.processDefinition;
        dv.setDefinition(d);

        dv.setCreateDate(new Date());
        dv.setCreateActor(user.getActor());
        dv.setVersion(dwvOld.processDefinition.getLatestVersion().getVersion() + 1);
        dv.setSubVersion(0L);
        processDefinitionVersionDao.create(dv);
        d.setLatestVersion(dv);
        log.debug("Process definition " + dwvOld + " was successfully redeployed");
        return new WfDefinition(parsed, true);
    }

    /**
     * Updates process definition, without incrementing version number.
     */
    public WfDefinition updateProcessDefinition(User user, Long processDefinitionVersionId, @NonNull byte[] processArchiveBytes) {
        val dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, dwv.processDefinition);
        val parsed = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(dwv.processDefinition.getName(), parsed.getName())) {
            throw new DefinitionNameMismatchException(dwv.processDefinition.getName(), parsed.getName());
        }
        checkCommentsOnDeploy(parseProcessDefinition(dwv.processDefinitionVersion.getContent()), parsed);
        dwv.processDefinitionVersion.setContent(parsed.getProcessDefinitionVersion().getContent());
        dwv.processDefinitionVersion.setUpdateDate(new Date());
        dwv.processDefinitionVersion.setUpdateActor(user.getActor());
        dwv.processDefinitionVersion.setSubVersion(dwv.processDefinitionVersion.getSubVersion() + 1);
        processDefinitionVersionDao.update(dwv.processDefinitionVersion);
        addUpdatedDefinitionInProcessLog(user, dwv);
        log.debug("Process definition " + dwv + " was successfully updated");
        return new WfDefinition(dwv);
    }

    private void checkCommentsOnDeploy(ParsedProcessDefinition oldDefinition, ParsedProcessDefinition definition) {
        boolean containsAllPreviousComments = definition.getChanges().containsAll(oldDefinition.getChanges());
        if (!SystemProperties.isDefinitionDeploymentWithCommentsCollisionsAllowed() && !containsAllPreviousComments) {
            throw new InternalApplicationException("The new version of definition must contain all version comments which exists in earlier "
                    + "uploaded definition. Most likely you try to upload an old version of definition (page update is recommended).");
        }
        if (!SystemProperties.isDefinitionDeploymentWithEmptyCommentsAllowed() &&
                containsAllPreviousComments &&
                definition.getChanges().size() == oldDefinition.getChanges().size()) {
            throw new InternalApplicationException("The new version of definition must contain more than "
                        + oldDefinition.getChanges().size() + " version comments. Uploaded definition contains " + definition.getChanges().size()
                        + " comments. Most likely you try to upload an old version of definition (page update is recommended). ");
        }
    }

    public void setProcessDefinitionSubprocessBindingDate(User user, Long processDefinitionVersionId, Date date) {
        ProcessDefinitionWithVersion dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, dwv.processDefinition);
        Date oldDate = dwv.processDefinitionVersion.getSubprocessBindingDate();
        dwv.processDefinitionVersion.setSubprocessBindingDate(date);
        processDefinitionVersionDao.update(dwv.processDefinitionVersion);
        log.info("ParsedProcessDefinition subprocessBindingDate changed: " + CalendarUtil.formatDateTime(oldDate) + " -> "
                + CalendarUtil.formatDateTime(date));
    }

    private void addUpdatedDefinitionInProcessLog(User user, ProcessDefinitionWithVersion dwv) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(dwv.processDefinition.getName());
        filter.setDefinitionVersion(dwv.processDefinitionVersion.getVersion());
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        for (CurrentProcess process : processes) {
            processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_CURRENT_PROCESS_VERSION), process, null);
        }
    }

    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        ParsedProcessDefinition definition = getLatestDefinition(definitionName);
        permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START, definition.getProcessDefinition()));
    }

    public WfDefinition getProcessDefinitionVersion(User user, String name, Long version) {
        ProcessDefinitionWithVersion dwv = processDefinitionDao.getByNameAndVersion(name, version);
        ParsedProcessDefinition definition = getDefinition(dwv.processDefinitionVersion.getId());
        permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START, definition.getProcessDefinition()));
    }

    public WfDefinition getProcessDefinition(User user, long processDefinitionVersionId) {
        try {
            val definition = getDefinition(processDefinitionVersionId);
            permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
            return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START, definition.getProcessDefinition()));
        } catch (Exception e) {
            val dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
            permissionDao.checkAllowed(user, Permission.LIST, dwv.processDefinition);
            return new WfDefinition(dwv);
        }
    }

    public ParsedProcessDefinition getParsedProcessDefinition(User user, long processDefinitionVersionId) {
        ParsedProcessDefinition pd = getDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.LIST, pd.getProcessDefinition());
        return pd;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, long processDefinitionVersionId, String subprocessId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<ProcessDefinitionWithVersion> dwvs = processDefinitionDao.findAllDefinitionVersions(name);
        if (dwvs.isEmpty() || !permissionDao.isAllowed(user, Permission.LIST, dwvs.get(0).processDefinition)) {
            return Collections.emptyList();
        }
        val result = new ArrayList<WfDefinition>(dwvs.size());
        for (val dwv : dwvs) {
            result.add(new WfDefinition(dwv));
        }
        return result;
    }

    public void undeployProcessDefinition(User user, @NonNull String definitionName, Long version) {
        ProcessDefinition d;
        ProcessDefinitionVersion dv;
        if (version == null) {
            d = processDefinitionDao.getByName(definitionName);
            dv = null;
        } else {
            // Load both definition and version by single SQL query.
            val dwv = processDefinitionDao.getByNameAndVersion(definitionName, version);
            d = dwv.processDefinition;
            dv = dwv.processDefinitionVersion;
        }

        // ===== Check if deletion allowed.

        permissionDao.checkAllowed(user, Permission.ALL, d);

        if (archivedProcessDao.processesExist(d.getId())) {
            throw new RuntimeException("Archived processes exist for definition ID=" + d.getId());
        }

        // Check that processes to be deleted don't have parent processes of different definition.
        String parentProcessDefinitionName = currentProcessDao.findParentProcessDefinitionName(d.getId());
        if (parentProcessDefinitionName != null) {
            throw new ParentProcessExistsException(definitionName, parentProcessDefinitionName);
        }

        // ===== Perform deletion.

        val processes = currentProcessDao.findAllProcessesForAllDefinitionVersions(d.getId());
        for (CurrentProcess p : processes) {
            deleteProcess(user, p);
        }
        currentProcessDao.flushPendingChanges();

        if (dv == null) {
            processDefinitionVersionDao.deleteAll(d.getId());
        } else {
            processDefinitionVersionDao.delete(dv);
        }

        permissionDao.deleteAllPermissions(d);
        processDefinitionDao.delete(d);
        systemLogDao.create(new ProcessDefinitionDeleteLog(user.getActor().getId(), d.getName(), dv == null ? null : dv.getVersion()));
        log.info("Process definition " + d + " successfully undeployed");
    }

    public List<ProcessDefinitionChange> getChanges(long processDefinitionVersionId) {
        List<Long> processDefinitionVersionIds = processDefinitionDao.findAllDefinitionVersionIds(processDefinitionVersionId, true);
        return getChanges(processDefinitionVersionIds);
    }

    public List<ProcessDefinitionChange> getLastChanges(long processDefinitionVersionId, int n) {
        Preconditions.checkArgument(n > 0);
        List<Long> processDefinitionVersionIds = processDefinitionDao.findAllDefinitionVersionIds(processDefinitionVersionId, false);
        if (n < processDefinitionVersionIds.size()) {
            processDefinitionVersionIds = new ArrayList<>(processDefinitionVersionIds.subList(0, n));
        }
        Collections.reverse(processDefinitionVersionIds);
        return getChanges(processDefinitionVersionIds);
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<Long> processDefinitionVersionIds = processDefinitionDao.findDefinitionVersionIds(definitionName, version1, version2);
        return getChanges(processDefinitionVersionIds);
    }

    public byte[] getFile(User user, long processDefinitionVersionId, String fileName) {
        ProcessDefinitionWithVersion dwv = processDefinitionDao.findDefinition(processDefinitionVersionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName) && !fileName.endsWith(FileDataProvider.BOTS_XML_FILE)) {
            permissionDao.checkAllowed(user, Permission.LIST, dwv.processDefinition);
        }
        if (FileDataProvider.PAR_FILE.equals(fileName)) {
            return dwv.processDefinitionVersion.getContent();
        }
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        return definition.getFileData(fileName);
    }

    public byte[] getGraph(User user, long processDefinitionVersionId, String subprocessId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        return definition.getGraphImageBytesNotNull();
    }

    public Interaction getStartInteraction(User user, long processDefinitionVersionId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        Interaction interaction = definition.getInteractionNotNull(definition.getStartStateNotNull().getNodeId());
        val defaultValues = definition.getDefaultVariableValues();
        for (val entry : defaultValues.entrySet()) {
            interaction.getDefaultVariableValues().put(entry.getKey(), entry.getValue());
        }
        return interaction;
    }

    public List<SwimlaneDefinition> getSwimlanes(User user, long processDefinitionVersionId) {
        ParsedProcessDefinition definition = processDefinitionLoader.getDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, long processDefinitionVersionId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, long processDefinitionVersionId, String variableName) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        permissionDao.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return definition.getVariable(variableName, true);
    }

    public int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation) {
        CompilerParameters parameters = CompilerParameters.createNonPaged();
        return new PresentationCompiler<ProcessDefinition>(batchPresentation).getCount(parameters);
    }

    /**
     * @param batchPresentation of type DEFINITIONS_HISTORY.
     */
    public List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        val result = new ArrayList<WfDefinition>();
        List<Long> processIdRestriction = getIdRestriction(user);
        if (processIdRestriction.isEmpty()) {
            return result;
        }
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addOwners(new RestrictionsToOwners(processIdRestriction, "definition.id"));
        List<ProcessDefinitionVersion> versions = new PresentationCompiler<ProcessDefinitionVersion>(batchPresentation).getBatch(parameters);
        for (ProcessDefinitionVersion dv : versions) {
            result.add(new WfDefinition(dv));
        }
        return result;
    }

    private ParsedProcessDefinition parseProcessDefinition(byte[] data) {
        try {
            val d = new ProcessDefinition();
            val dv = new ProcessDefinitionVersion();
            dv.setDefinition(d);
            dv.setContent(data);
            ProcessArchive archive = new ProcessArchive(d, dv);
            return archive.parseProcessDefinition();
        } catch (DefinitionArchiveFormatException e) {
            throw e;
        } catch (Throwable e) {
            throw new DefinitionArchiveFormatException(e);
        }
    }

    /**
     * @param batchPresentation of type DEFINITIONS.
     */
    public List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        List<Long> definitionIdRestriction = getIdRestriction(user);
        if (definitionIdRestriction.isEmpty()) {
            return Lists.newArrayList();
        }

        CompilerParameters parameters = CompilerParameters.create(enablePaging)
                .loadOnlySpecificHqlFields("latestVersion.id")
                .addOwners(new RestrictionsToOwners(definitionIdRestriction, "id"));
        List<Number> definitionVersionIds = new PresentationCompiler<Number>(batchPresentation).getBatch(parameters);
        val processDefinitions = new HashMap<ProcessDefinition, ParsedProcessDefinition>(definitionVersionIds.size());
        val definitions = new ArrayList<ProcessDefinition>(definitionVersionIds.size());
        for (Number definitionVersionId : definitionVersionIds) {
            try {
                ParsedProcessDefinition parsed = getDefinition(definitionVersionId.longValue());
                processDefinitions.put(parsed.getProcessDefinition(), parsed);
                definitions.add(parsed.getProcessDefinition());
            } catch (Exception e) {
                ProcessDefinition processDefinition = processDefinitionDao.get(definitionVersionId.longValue());
                if (processDefinition != null) {
                    processDefinitions.put(processDefinition, null);
                    definitions.add(processDefinition);
                }
            }
        }
        val result = new ArrayList<WfDefinition>(definitionVersionIds.size());
        isPermissionAllowed(user, definitions, Permission.START, new StartProcessPermissionCheckCallback(result, processDefinitions));
        return result;
    }

    private List<Long> getIdRestriction(User user) {
        val allIds = processDefinitionDao.findAllDefinitionIds();
        val idsWithPermission = new ArrayList<Long>();
        isPermissionAllowed(user, SecuredObjectType.DEFINITION, allIds, Permission.LIST, new CheckMassPermissionCallback<Long>() {
            @Override
            public void onPermissionGranted(Long id) {
                idsWithPermission.add(id);
            }
        });
        return idsWithPermission;
    }

    private List<ProcessDefinitionChange> getChanges(List<Long> processDefinitionVersionIds) {
        List<ProcessDefinitionChange> ignoredChanges = null;
        if (!processDefinitionVersionIds.isEmpty()) {
            ParsedProcessDefinition firstParsed = getDefinition(processDefinitionVersionIds.get(0));
            long firstVersion = firstParsed.getProcessDefinitionVersion().getVersion();
            Long previousVersionId = processDefinitionDao.findDefinitionVersionIdLatestVersionLessThan(
                    firstParsed.getProcessDefinition().getId(),
                    firstVersion
            );
            if (previousVersionId != null) {
                ignoredChanges = getDefinition(previousVersionId).getChanges();
            }
        }
        val result = new ArrayList<ProcessDefinitionChange>();
        for (val processDefinitionVersionId : processDefinitionVersionIds) {
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(processDefinitionVersionId);
            for (ProcessDefinitionChange change : parsedProcessDefinition.getChanges()) {
                if (ignoredChanges != null && ignoredChanges.contains(change) || result.contains(change)) {
                    continue;
                }
                result.add(new ProcessDefinitionChange(parsedProcessDefinition.getProcessDefinitionVersion().getVersion(), change));
            }
        }
        return result;
    }

    private static final class StartProcessPermissionCheckCallback extends CheckMassPermissionCallback<SecuredObject> {
        private final List<WfDefinition> result;
        private final Map<ProcessDefinition, ParsedProcessDefinition> parsedDefinitions;

        private StartProcessPermissionCheckCallback(List<WfDefinition> result, Map<ProcessDefinition, ParsedProcessDefinition> parsedDefinitions) {
            this.result = result;
            this.parsedDefinitions = parsedDefinitions;
        }

        @Override
        public void onPermissionGranted(SecuredObject securedObject) {
            addDefinitionToResult(securedObject, true);
        }

        @Override
        public void onPermissionDenied(SecuredObject securedObject) {
            addDefinitionToResult(securedObject, false);
        }

        private void addDefinitionToResult(SecuredObject securedObject, boolean canBeStarted) {
            val d = (ProcessDefinition) securedObject;
            ParsedProcessDefinition parsed = parsedDefinitions.get(d);
            if (parsed != null) {
                result.add(new WfDefinition(parsed, canBeStarted));
            } else {
                result.add(new WfDefinition(d, d.getLatestVersion()));
            }
        }
    }
}
