package ru.runa.wfe.definition.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.definition.update.ProcessDefinitionUpdateManager;
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
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

@Component
public class ProcessDefinitionLogic extends WfCommonLogic {
    @Autowired
    private ProcessDefinitionUpdateManager processDefinitionUpdateManager;

    /**
     * @param secondsBeforeArchiving
     *              If null or negative, will be nulled in database (default will be used).
     */
    public WfDefinition deployProcessDefinition(User user, byte[] processArchiveBytes, List<String> categories, Integer secondsBeforeArchiving) {
        permissionDao.checkAllowed(user, Permission.CREATE_DEFINITION, SecuredSingleton.SYSTEM);
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
        ProcessDefinitionPack p = new ProcessDefinitionPack();
        ProcessDefinition d = new ProcessDefinition();
        p.setLanguage(parsed.getLanguage());
        p.setName(parsed.getName());
        p.setDescription(parsed.getDescription());
        p.setCategories(categories);
        p.setSecondsBeforeArchiving(secondsBeforeArchiving);
        d.setPack(p);
        d.setContent(processArchiveBytes);
        d.setCreateDate(new Date());
        d.setCreateActor(user.getActor());
        d.setVersion(1L);
        d.setSubVersion(0L);
        processDefinitionPackDao.create(p);
        processDefinitionDao.create(d);
        p.setLatest(d);
        permissionDao.setPermissions(user.getActor(), ApplicablePermissions.listVisible(SecuredObjectType.DEFINITION), p);
        log.debug("Deployed process definition " + parsed);
        return new WfDefinition(d);
    }

    /**
     * Redeploys process definition by name, by creating new definition version.
     *
     * @param secondsBeforeArchiving
     *              If null, old value will be used (compatibility mode); if negative, will be nulled in database (default will be used).
     */
    public WfDefinition redeployProcessDefinition(User user, Long processDefinitionId, byte[] processArchiveBytes, List<String> categories,
            Integer secondsBeforeArchiving) {
        ProcessDefinition oldDefinition = processDefinitionDao.get(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, oldDefinition.getPack());
        if (processArchiveBytes == null) {
            Preconditions.checkNotNull(categories, "In mode 'update only categories' categories are required");
            oldDefinition.getPack().setCategories(categories);
            if (secondsBeforeArchiving != null) {
                oldDefinition.getPack().setSecondsBeforeArchiving(secondsBeforeArchiving < 0 ? null : secondsBeforeArchiving);
            }
            return getProcessDefinition(user, processDefinitionId);
        }

        if (!Objects.equals(oldDefinition.getId(), oldDefinition.getPack().getLatest().getId())) {
            throw new InternalApplicationException("Latest version of process definition '" + oldDefinition.getPack().getName() + "' is '"
                    + oldDefinition.getPack().getLatest().getVersion() + "'. You provided processDefinitionId for version '"
                    + oldDefinition.getVersion() + "'");
        }

        ParsedProcessDefinition parsed = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(oldDefinition.getPack().getName(), parsed.getName())) {
            throw new DefinitionNameMismatchException(oldDefinition.getPack().getName(), parsed.getName());
        }
        ParsedProcessDefinition parsedOld = parseProcessDefinition(oldDefinition.getContent());
        try {
            checkCommentsOnDeploy(parsedOld, parsed);
        } catch (InvalidDefinitionException e) {
            log.warn(oldDefinition + ": " + e);
        }
        ProcessDefinitionPack p = oldDefinition.getPack();
        p.setDescription(parsed.getDescription());
        p.setLanguage(parsed.getLanguage());
        if (categories != null) {
            p.setCategories(categories);
        }
        if (secondsBeforeArchiving != null) {
            p.setSecondsBeforeArchiving(secondsBeforeArchiving < 0 ? null : secondsBeforeArchiving);
        }
        ProcessDefinition d = new ProcessDefinition();
        d.setPack(p);
        d.setContent(processArchiveBytes);
        d.setCreateDate(new Date());
        d.setCreateActor(user.getActor());
        d.setVersion(oldDefinition.getPack().getLatest().getVersion() + 1);
        d.setSubVersion(0L);
        processDefinitionDao.create(d);
        p.setLatest(d);
        log.debug("Process definition " + oldDefinition + " was successfully redeployed");
        return new WfDefinition(d);
    }

    /**
     * Updates process definition subversion.
     */
    public WfDefinition updateProcessDefinition(User user, Long processDefinitionId, @NonNull byte[] processArchiveBytes) {
        val d = processDefinitionDao.get(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, d.getPack());
        val parsed = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(d.getPack().getName(), parsed.getName())) {
            throw new DefinitionNameMismatchException(d.getPack().getName(), parsed.getName());
        }
        ParsedProcessDefinition oldDefinition = getDefinition(d.getId());
        checkCommentsOnDeploy(oldDefinition, parsed);
        List<CurrentProcess> processes = processDefinitionUpdateManager.findApplicableProcesses(oldDefinition);
        Set<CurrentProcess> affectedProcesses = processDefinitionUpdateManager.before(oldDefinition, parsed, processes);
        d.setContent(processArchiveBytes);
        d.setUpdateDate(new Date());
        d.setUpdateActor(user.getActor());
        d.setSubVersion(d.getSubVersion() + 1);
        processDefinitionDao.update(d);
        addUpdatedDefinitionInProcessLog(user, d);
        processDefinitionUpdateManager.after(parsed, affectedProcesses);
        log.debug("Process definition " + d + " was successfully updated");
        return new WfDefinition(d);
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

    public void setProcessDefinitionSubprocessBindingDate(User user, Long processDefinitionId, Date date) {
        ProcessDefinition d = processDefinitionDao.get(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, d.getPack());
        Date oldDate = d.getSubprocessBindingDate();
        d.setSubprocessBindingDate(date);
        processDefinitionDao.update(d);
        log.info("ParsedProcessDefinition subprocessBindingDate changed: " + CalendarUtil.formatDateTime(oldDate) + " -> "
                + CalendarUtil.formatDateTime(date));
    }

    private void addUpdatedDefinitionInProcessLog(User user, ProcessDefinition d) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(d.getPack().getName());
        filter.setDefinitionVersion(d.getVersion());
        filter.setFinished(false);
        List<CurrentProcess> processes = currentProcessDao.getProcesses(filter);
        for (CurrentProcess process : processes) {
            processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPGRADE_CURRENT_PROCESS_VERSION), process, null);
        }
    }

    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        ParsedProcessDefinition definition = getLatestDefinition(definitionName);
        permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START_PROCESS, definition.getSecuredObject()));
    }

    public WfDefinition getProcessDefinitionVersion(User user, String name, Long version) {
        ProcessDefinition d = processDefinitionDao.getByNameAndVersion(name, version);
        ParsedProcessDefinition definition = getDefinition(d.getId());
        permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START_PROCESS, definition.getSecuredObject()));
    }

    public WfDefinition getProcessDefinition(User user, Long processDefinitionId) {
        try {
            val definition = getDefinition(processDefinitionId);
            permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
            return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START_PROCESS, definition.getSecuredObject()));
        } catch (Exception e) {
            val d = processDefinitionDao.get(processDefinitionId);
            permissionDao.checkAllowed(user, Permission.READ, d.getPack());
            return new WfDefinition(d);
        }
    }

    public ParsedProcessDefinition getParsedProcessDefinition(User user, Long processDefinitionId) {
        ParsedProcessDefinition pd = getDefinition(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.READ, pd.getSecuredObject());
        return pd;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long processDefinitionId, String subprocessId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<ProcessDefinition> list = processDefinitionDao.findAllByNameOrderByVersionDesc(name);
        if (list.isEmpty() || !permissionDao.isAllowed(user, Permission.READ, list.get(0).getPack())) {
            return Collections.emptyList();
        }
        return list.stream().map(d -> new WfDefinition(d)).collect(Collectors.toList());
    }

    public void undeployProcessDefinition(User user, @NonNull String definitionName, Long version) {
        ProcessDefinitionPack p;
        ProcessDefinition d;
        if (version == null) {
            p = processDefinitionPackDao.getByName(definitionName);
            d = null;
        } else {
            // Load both definition and version by single SQL query.
            d = processDefinitionDao.getByNameAndVersion(definitionName, version);
            p = d.getPack();
        }

        // ===== Check if deletion allowed.

        permissionDao.checkAllowed(user, Permission.DELETE, p);

        if (archivedProcessDao.processesExist(p)) {
            throw new RuntimeException("Archived processes exist for definition ID=" + p.getId());
        }

        // Check that processes to be deleted don't have parent processes of different definition.
        String parentProcessDefinitionName = currentProcessDao.findParentProcessDefinitionName(p);
        if (parentProcessDefinitionName != null) {
            throw new ParentProcessExistsException(definitionName, parentProcessDefinitionName);
        }

        // ===== Perform deletion.

        val processes = currentProcessDao.findAllProcessesForAllDefinitionVersions(p);
        for (CurrentProcess cp : processes) {
            deleteProcess(user, cp);
        }
        currentProcessDao.flushPendingChanges();

        if (d == null) {
            processDefinitionDao.deleteAll(p);
        } else {
            processDefinitionDao.delete(d);
        }

        permissionDao.deleteAllPermissions(p);
        processDefinitionPackDao.delete(p);
        systemLogDao.create(new ProcessDefinitionDeleteLog(user.getActor().getId(), p.getName(), d == null ? null : d.getVersion()));
        log.info("Process definition " + p + " successfully undeployed");
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<Long> processDefinitionIds = processDefinitionDao.findIds(definitionName, version1, version2);
        return getChanges(processDefinitionIds);
    }

    public byte[] getFile(User user, Long processDefinitionId, String fileName) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName) && !fileName.endsWith(FileDataProvider.BOTS_XML_FILE)) {
            permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        }
        if (FileDataProvider.PAR_FILE.equals(fileName)) {
            return processDefinitionDao.get(processDefinitionId).getContent();
        }
        return definition.getFileData(fileName);
    }

    public byte[] getGraph(User user, Long processDefinitionId, String subprocessId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionId);
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        return definition.getGraphImageBytesNotNull();
    }

    public Interaction getStartInteraction(User user, Long processDefinitionId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionId);
        Interaction interaction = definition.getInteractionNotNull(definition.getStartStateNotNull().getNodeId());
        Map<String, Object> defaultValues = definition.getDefaultVariableValues();
        for (Entry<String, Object> entry : defaultValues.entrySet()) {
            interaction.getDefaultVariableValues().put(entry.getKey(), entry.getValue());
        }
        return interaction;
    }

    public List<SwimlaneDefinition> getSwimlanes(User user, Long processDefinitionId) {
        ParsedProcessDefinition definition = processDefinitionLoader.getDefinition(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, Long processDefinitionId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, Long processDefinitionId, String variableName) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getSecuredObject());
        return definition.getVariable(variableName, true);
    }

    public int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation) {
        CompilerParameters parameters = CompilerParameters.createNonPaged();
        return new PresentationCompiler<>(batchPresentation).getCount(parameters);
    }

    /**
     * @param batchPresentation of type DEFINITIONS_HISTORY.
     */
    public List<WfDefinition> getProcessDefinitionsNotUsingCache(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        val result = new ArrayList<WfDefinition>();
        List<Long> processIdRestriction = getPackIdRestriction(user);
        if (processIdRestriction.isEmpty()) {
            return result;
        }
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addOwners(new RestrictionsToOwners(processIdRestriction, "pack.id"));
        List<ProcessDefinition> definitions = new PresentationCompiler<ProcessDefinition>(batchPresentation).getBatch(parameters);
        for (ProcessDefinition d : definitions) {
            result.add(new WfDefinition(d));
        }
        return result;
    }

    private ParsedProcessDefinition parseProcessDefinition(byte[] data) {
        try {
            val d = new ProcessDefinition();
            d.setPack(new ProcessDefinitionPack());
            d.setContent(data);
            ProcessArchive archive = new ProcessArchive(d);
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
        List<Long> definitionIdRestriction = getPackIdRestriction(user);
        if (definitionIdRestriction.isEmpty()) {
            return Lists.newArrayList();
        }

        CompilerParameters parameters = CompilerParameters.create(enablePaging)
                .loadOnlySpecificHqlFields("latest.id")
                .addOwners(new RestrictionsToOwners(definitionIdRestriction, "id"));
        List<Number> definitionIds = new PresentationCompiler<Number>(batchPresentation).getBatch(parameters);
        val processDefinitions = new HashMap<SecuredObject, ParsedProcessDefinition>(definitionIds.size());
        val securedObjects = new ArrayList<SecuredObject>(definitionIds.size());
        for (Number definitionId : definitionIds) {
            try {
                ParsedProcessDefinition parsed = getDefinition(definitionId.longValue());
                processDefinitions.put(parsed.getSecuredObject(), parsed);
                securedObjects.add(parsed.getSecuredObject());
            } catch (Exception e) {
                ProcessDefinitionPack processDefinitionPack = processDefinitionPackDao.get(definitionId.longValue());
                if (processDefinitionPack != null) {
                    securedObjects.add(processDefinitionPack);
                }
            }
        }
        val result = new ArrayList<WfDefinition>(definitionIds.size());
        isPermissionAllowed(user, securedObjects, Permission.START_PROCESS, new StartProcessPermissionCheckCallback(result, processDefinitions));
        return result;
    }

    private List<Long> getPackIdRestriction(User user) {
        val allIds = processDefinitionPackDao.findAllIds();
        val idsWithPermission = new ArrayList<Long>();
        isPermissionAllowed(user, SecuredObjectType.DEFINITION, allIds, Permission.READ, new CheckMassPermissionCallback<Long>() {
            @Override
            public void onPermissionGranted(Long id) {
                idsWithPermission.add(id);
            }
        });
        return idsWithPermission;
    }

    private List<ProcessDefinitionChange> getChanges(List<Long> processDefinitionIds) {
        List<ProcessDefinitionChange> ignoredChanges = null;
        if (!processDefinitionIds.isEmpty()) {
            ParsedProcessDefinition firstParsed = getDefinition(processDefinitionIds.get(0));
            Long firstVersion = firstParsed.getVersion();
            Long previousId = processDefinitionDao.findLatestIdLessThan(firstParsed.getName(), firstVersion);
            if (previousId != null) {
                ignoredChanges = getDefinition(previousId).getChanges();
            }
        }
        val result = new ArrayList<ProcessDefinitionChange>();
        for (val processDefinitionId : processDefinitionIds) {
            ParsedProcessDefinition parsedProcessDefinition = getDefinition(processDefinitionId);
            for (ProcessDefinitionChange change : parsedProcessDefinition.getChanges()) {
                if (ignoredChanges != null && ignoredChanges.contains(change) || result.contains(change)) {
                    continue;
                }
                result.add(new ProcessDefinitionChange(parsedProcessDefinition.getVersion(), change));
            }
        }
        return result;
    }

    private static final class StartProcessPermissionCheckCallback extends CheckMassPermissionCallback<SecuredObject> {
        private final List<WfDefinition> result;
        private final Map<SecuredObject, ParsedProcessDefinition> parsedDefinitions;

        private StartProcessPermissionCheckCallback(List<WfDefinition> result, Map<SecuredObject, ParsedProcessDefinition> parsedDefinitions) {
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
            ParsedProcessDefinition parsed = parsedDefinitions.get(securedObject);
            if (parsed != null) {
                result.add(new WfDefinition(parsed, canBeStarted));
            } else {
                val p = (ProcessDefinitionPack) securedObject;
                result.add(new WfDefinition(p, p.getLatest()));
            }
        }
    }
}
