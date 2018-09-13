/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.definition.logic;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CheckMassPermissionCallback;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.Process;
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

public class DefinitionLogic extends WFCommonLogic {

    public WfDefinition deployProcessDefinition(User user, byte[] processArchiveBytes, List<String> categories) {
        permissionDAO.checkAllowed(user, Permission.CREATE, SecuredSingleton.DEFINITIONS);
        ParsedProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        try {
            getLatestDefinition(definition.getName());
            throw new DefinitionAlreadyExistException(definition.getName());
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
        ProcessDefinition d = definition.getProcessDefinition();
        ProcessDefinitionVersion dv = definition.getProcessDefinitionVersion();
        d.setCategories(categories);
        dv.setCreateDate(new Date());
        dv.setCreateActor(user.getActor());
        dv.setVersion(1L);
        deploymentDao.create(d);
        deploymentVersionDAO.create(dv);
        permissionDAO.setPermissions(user.getActor(), Collections.singletonList(Permission.ALL), d);
        log.debug("Deployed process definition " + definition);
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, d));
    }

    /**
     * Adds new definition version.
     */
    public WfDefinition redeployProcessDefinition(User user, long processDefinitionVersionId, byte[] processArchiveBytes, List<String> categories) {
        ProcessDefinitionWithVersion dwvOld = deploymentDao.findDeployment(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, dwvOld.processDefinition);
        if (processArchiveBytes == null) {
            Preconditions.checkNotNull(categories, "In mode 'update only categories' categories are required");
            dwvOld.processDefinition.setCategories(categories);
            return getProcessDefinition(user, processDefinitionVersionId);
        }

        ProcessDefinitionWithVersion dwvOldLatest = deploymentDao.findLatestDeployment(dwvOld.processDefinition.getId());
        if (!Objects.equals(dwvOldLatest.processDefinitionVersion.getId(), dwvOld.processDefinitionVersion.getId())) {
            throw new InternalApplicationException("Last deployed version of process definition '" + dwvOldLatest.processDefinition.getName() + "' is '" +
                    dwvOldLatest.processDefinitionVersion.getVersion() + "'. You provided process definition id for version '" +
                    dwvOld.processDefinitionVersion.getVersion() + "'");
        }

        ParsedProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(dwvOld.processDefinition.getName(), definition.getName())) {
            throw new DefinitionNameMismatchException(dwvOld.processDefinition.getName(), definition.getName());
        }
        ProcessDefinition d = definition.getProcessDefinition();
        ProcessDefinitionVersion dv = definition.getProcessDefinitionVersion();
        if (categories != null) {
            d.setCategories(categories);
        } else {
            d.setCategory(dwvOld.processDefinition.getCategory());
        }
        try {
            checkCommentsOnDeploy(parseProcessDefinition(dwvOld.processDefinitionVersion.getContent()), definition);
        } catch (InvalidDefinitionException e) {
            log.warn(dwvOld + ": " + e);
        }
        dv.setCreateDate(new Date());
        dv.setCreateActor(user.getActor());
        dv.setVersion(dwvOldLatest.processDefinitionVersion.getVersion() + 1);
        deploymentDao.create(d);
        deploymentVersionDAO.create(dv);
        log.debug("Process definition " + dwvOld + " was successfully redeployed");
        return new WfDefinition(definition, true);
    }

    /**
     * Updates process definition (same version).
     */
    public WfDefinition updateProcessDefinition(User user, Long processDefinitionVersionId, @NonNull byte[] processArchiveBytes) {
        val dwv = deploymentDao.findDeployment(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, dwv.processDefinition);
        val definition = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(dwv.processDefinition.getName(), definition.getName())) {
            throw new DefinitionNameMismatchException(dwv.processDefinition.getName(), definition.getName());
        }
        checkCommentsOnDeploy(parseProcessDefinition(dwv.processDefinitionVersion.getContent()), definition);
        dwv.processDefinitionVersion.setContent(definition.getProcessDefinitionVersion().getContent());
        dwv.processDefinitionVersion.setUpdateDate(new Date());
        dwv.processDefinitionVersion.setUpdateActor(user.getActor());
        deploymentVersionDAO.update(dwv.processDefinitionVersion);
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
        ProcessDefinitionWithVersion dwv = deploymentDao.findDeployment(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, dwv.processDefinition);
        Date oldDate = dwv.processDefinitionVersion.getSubprocessBindingDate();
        dwv.processDefinitionVersion.setSubprocessBindingDate(date);
        deploymentVersionDAO.update(dwv.processDefinitionVersion);
        log.info("ParsedProcessDefinition subprocessBindingDate changed: " + CalendarUtil.formatDateTime(oldDate) + " -> "
                + CalendarUtil.formatDateTime(date));
    }

    private void addUpdatedDefinitionInProcessLog(User user, ProcessDefinitionWithVersion dwv) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(dwv.processDefinition.getName());
        filter.setDefinitionVersion(dwv.processDefinitionVersion.getVersion());
        List<Process> processes = processDao.getProcesses(filter);
        for (Process process : processes) {
            processLogDAO.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_CURRENT_PROCESS_VERSION), process, null);
        }
    }

    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        ParsedProcessDefinition definition = getLatestDefinition(definitionName);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getProcessDefinition()));
    }

    public WfDefinition getProcessDefinitionVersion(User user, String name, Long version) {
        ProcessDefinitionWithVersion dwv = deploymentDao.findDeployment(name, version);
        ParsedProcessDefinition definition = getDefinition(dwv.processDefinitionVersion.getId());
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getProcessDefinition()));
    }

    public WfDefinition getProcessDefinition(User user, long processDefinitionVersionId) {
        try {
            val definition = getDefinition(processDefinitionVersionId);
            permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
            return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getProcessDefinition()));
        } catch (Exception e) {
            val dwv = deploymentDao.findDeployment(processDefinitionVersionId);
            permissionDAO.checkAllowed(user, Permission.LIST, dwv.processDefinition);
            return new WfDefinition(dwv);
        }
    }

    public ParsedProcessDefinition getParsedProcessDefinition(User user, long processDefinitionVersionId) {
        ParsedProcessDefinition pd = getDefinition(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, pd.getProcessDefinition());
        return pd;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, long processDefinitionVersionId, String subprocessId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<ProcessDefinitionWithVersion> dwvs = deploymentDao.findAllDeploymentVersions(name);
        if (dwvs.isEmpty() || !permissionDAO.isAllowed(user, Permission.LIST, dwvs.get(0).processDefinition)) {
            return Collections.emptyList();
        }
        val result = new ArrayList<WfDefinition>(dwvs.size());
        for (val dwv : dwvs) {
            result.add(new WfDefinition(dwv));
        }
        return result;
    }

    public void undeployProcessDefinition(User user, String definitionName, Long version) {
        Preconditions.checkNotNull(definitionName, "definitionName must be specified.");

        // TODO Can be optimized to single SQL statement.
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(definitionName);
        filter.setDefinitionVersion(version);
        List<Process> processes = processDao.getProcesses(filter);
        for (Process process : processes) {
            NodeProcess parent = nodeProcessDAO.findBySubProcessId(process.getId());
            if (parent != null) {
                throw new ParentProcessExistsException(definitionName, parent.getProcess().getProcessDefinitionVersion().getProcessDefinition().getName());
            }
        }

        if (version == null) {
            ProcessDefinition latestProcessDefinition = deploymentDao.findLatestDeployment(definitionName);
            permissionDAO.checkAllowed(user, Permission.ALL, latestProcessDefinition);
            permissionDAO.deleteAllPermissions(latestProcessDefinition);
            List<ProcessDefinition> processDefinitions = deploymentDao.findAllDeploymentVersions(definitionName);
            for (ProcessDefinition processDefinition : processDefinitions) {
                removeDeployment(user, processDefinition);
            }
            log.info("Process definition " + latestProcessDefinition + " successfully undeployed");
        } else {
            ProcessDefinitionWithVersion dwv = deploymentDao.findDeployment(definitionName, version);
            removeDeployment(user, deployment);
            log.info("Process definition " + deployment + " successfully undeployed");
        }
    }

    private void removeDeployment(User user, ProcessDefinition processDefinition) {
        List<Process> processes = processDao.findAllProcesses(processDefinition.getId());
        for (Process process : processes) {
            deleteProcess(user, process);
        }
        deploymentVersionDAO.deleteAll(processDefinition);  // TODO Wrong, this is about deleting SINGLE version!!!
        deploymentDao.delete(processDefinition);
        systemLogDAO.create(new ProcessDefinitionDeleteLog(user.getActor().getId(), processDefinition.getName(), processDefinition.getVersion()));
    }

    public List<ProcessDefinitionChange> getChanges(long processDefinitionVersionId) {
        List<Long> processDefinitionVersionIds = deploymentDao.findAllDeploymentVersionIds(processDefinitionVersionId, true);
        return getChanges(processDefinitionVersionIds);
    }

    public List<ProcessDefinitionChange> getLastChanges(long processDefinitionVersionId, int n) {
        Preconditions.checkArgument(n > 0);
        List<Long> processDefinitionVersionIds = deploymentDao.findAllDeploymentVersionIds(processDefinitionVersionId, false);
        if (n < processDefinitionVersionIds.size()) {
            processDefinitionVersionIds = new ArrayList<>(processDefinitionVersionIds.subList(0, n));
        }
        Collections.reverse(processDefinitionVersionIds);
        return getChanges(processDefinitionVersionIds);
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<Long> processDefinitionVersionIds = deploymentDao.findDeploymentVersionIds(definitionName, version1, version2);
        return getChanges(processDefinitionVersionIds);
    }

    public byte[] getFile(User user, long processDefinitionVersionId, String fileName) {
        ProcessDefinitionWithVersion dwv = deploymentDao.findDeployment(processDefinitionVersionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName) && !fileName.endsWith(IFileDataProvider.BOTS_XML_FILE)) {
            permissionDAO.checkAllowed(user, Permission.LIST, dwv.processDefinition);
        }
        if (IFileDataProvider.PAR_FILE.equals(fileName)) {
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
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, long processDefinitionVersionId) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, long processDefinitionVersionId, String variableName) {
        ParsedProcessDefinition definition = getDefinition(processDefinitionVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getProcessDefinition());
        return definition.getVariable(variableName, true);
    }

    public List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        CompilerParameters parameters = CompilerParameters.create(enablePaging).loadOnlyIdentity();
        return getProcessDefinitions(user, batchPresentation, parameters);
    }

    public int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation) {
        CompilerParameters parameters = CompilerParameters.createNonPaged();
        return new PresentationCompiler<ProcessDefinition>(batchPresentation).getCount(parameters);
    }

    public List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        List<String> processNameRestriction = getProcessNameRestriction(user);
        if (processNameRestriction.isEmpty()) {
            return Lists.newArrayList();
        }
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addOwners(new RestrictionsToOwners(processNameRestriction, "name"));
        List<ProcessDefinition> processDefinitions = new PresentationCompiler<ProcessDefinition>(batchPresentation).getBatch(parameters);
        List<WfDefinition> definitions = Lists.newArrayList();
        for (ProcessDefinition processDefinition : processDefinitions) {
            definitions.add(new WfDefinition(processDefinition));
        }
        return definitions;
    }

    private ParsedProcessDefinition parseProcessDefinition(byte[] data) {
        try {
            val d = new ProcessDefinition();
            val dv = new ProcessDefinitionVersion();
            dv.setProcessDefinition(d);
            dv.setContent(data);
            ProcessArchive archive = new ProcessArchive(d, dv);
            return archive.parseProcessDefinition();
        } catch (DefinitionArchiveFormatException e) {
            throw e;
        } catch (Throwable e) {
            throw new DefinitionArchiveFormatException(e);
        }
    }

    private List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, CompilerParameters parameters) {
        List<String> processNameRestriction = getProcessNameRestriction(user);
        if (processNameRestriction.isEmpty()) {
            return Lists.newArrayList();
        }
        parameters = parameters.addOwners(new RestrictionsToOwners(processNameRestriction, "name"));
        List<Number> deploymentIds = new PresentationCompiler<Number>(batchPresentation).getBatch(parameters);
        final Map<ProcessDefinition, ParsedProcessDefinition> processDefinitions = Maps.newHashMapWithExpectedSize(deploymentIds.size());
        List<ProcessDefinition> deployments = Lists.newArrayListWithExpectedSize(deploymentIds.size());
        for (Number definitionId : deploymentIds) {
            try {
                ParsedProcessDefinition definition = getDefinition(definitionId.longValue());
                processDefinitions.put(definition.getProcessDefinition(), definition);
                deployments.add(definition.getProcessDefinition());
            } catch (Exception e) {
                ProcessDefinition processDefinition = deploymentDao.get(definitionId.longValue());
                if (processDefinition != null) {
                    processDefinitions.put(processDefinition, null);
                    deployments.add(processDefinition);
                }
            }
        }
        final List<WfDefinition> result = Lists.newArrayListWithExpectedSize(deploymentIds.size());
        isPermissionAllowed(user, deployments, Permission.START,
                new StartProcessPermissionCheckCallback(result, processDefinitions));
        return result;
    }

    private List<String> getProcessNameRestriction(User user) {
        List<ru.runa.wfe.definition.logic.DefinitionLogic.DefinitionSecuredObject> definitionSecuredObjects = new ArrayList<>();
        for (String deploymentName : deploymentDao.findDeploymentNames()) {
            definitionSecuredObjects.add(new ru.runa.wfe.definition.logic.DefinitionLogic.DefinitionSecuredObject(deploymentName));
        }
        final List<String> definitionsWithPermission = new ArrayList<>();
        isPermissionAllowed(user, definitionSecuredObjects, Permission.LIST, new CheckMassPermissionCallback() {
            @Override
            public void onPermissionGranted(SecuredObject securedObject) {
                definitionsWithPermission.add(((ru.runa.wfe.definition.logic.DefinitionLogic.DefinitionSecuredObject) securedObject).getDeploymentName());
            }
        });
        return definitionsWithPermission;
    }

    private List<ProcessDefinitionChange> getChanges(List<Long> processDefinitionVersionIds) {
        List<ProcessDefinitionChange> ignoredChanges = null;
        if (!processDefinitionVersionIds.isEmpty()) {
            ParsedProcessDefinition firstDefinition = getDefinition(processDefinitionVersionIds.get(0));
            long firstDeploymentVersion = firstDefinition.getProcessDefinitionVersion().getVersion();
            Long previousDeploymentVersionId = deploymentDao.findDeploymentVersionIdLatestVersionLessThan(
                    firstDefinition.getProcessDefinition().getId(),
                    firstDeploymentVersion
            );
            if (previousDeploymentVersionId != null) {
                ignoredChanges = getDefinition(previousDeploymentVersionId).getChanges();
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

    private static final class DefinitionSecuredObject extends SecuredObject {

        private static final long serialVersionUID = 1L;
        private final String deploymentName;

        public DefinitionSecuredObject(String deploymentName) {
            super();
            this.deploymentName = deploymentName;
        }

        @Override
        public Long getIdentifiableId() {
            return (long) deploymentName.hashCode();
        }

        @Override
        public SecuredObjectType getSecuredObjectType() {
            return SecuredObjectType.DEFINITION;
        }

        public String getDeploymentName() {
            return deploymentName;
        }
    }

    private static final class StartProcessPermissionCheckCallback extends CheckMassPermissionCallback {
        private final List<WfDefinition> result;
        private final Map<ProcessDefinition, ParsedProcessDefinition> processDefinitions;

        private StartProcessPermissionCheckCallback(List<WfDefinition> result, Map<ProcessDefinition, ParsedProcessDefinition> processDefinitions) {
            this.result = result;
            this.processDefinitions = processDefinitions;
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
            ParsedProcessDefinition definition = processDefinitions.get(securedObject);
            if (definition != null) {
                result.add(new WfDefinition(definition, canBeStarted));
            } else {
                result.add(new WfDefinition((ProcessDefinition) securedObject));
            }
        }
    }
}
