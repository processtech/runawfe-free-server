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
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentVersion;
import ru.runa.wfe.definition.DeploymentWithVersion;
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
import ru.runa.wfe.lang.ProcessDefinition;
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
        ProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        try {
            getLatestDefinition(definition.getName());
            throw new DefinitionAlreadyExistException(definition.getName());
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
        Deployment d = definition.getDeployment();
        DeploymentVersion dv = definition.getDeploymentVersion();
        d.setCategories(categories);
        dv.setCreateDate(new Date());
        dv.setCreateActor(user.getActor());
        dv.setVersion(1L);
        deploymentDAO.create(d);
        deploymentVersionDAO.create(dv);
        permissionDAO.setPermissions(user.getActor(), Collections.singletonList(Permission.ALL), d);
        log.debug("Deployed process definition " + definition);
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, d));
    }

    /**
     * Adds new definition version.
     */
    public WfDefinition redeployProcessDefinition(User user, long deploymentVersionId, byte[] processArchiveBytes, List<String> categories) {
        DeploymentWithVersion dwvOld = deploymentDAO.findDeployment(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, dwvOld.deployment);
        if (processArchiveBytes == null) {
            Preconditions.checkNotNull(categories, "In mode 'update only categories' categories are required");
            dwvOld.deployment.setCategories(categories);
            return getProcessDefinition(user, deploymentVersionId);
        }

        DeploymentWithVersion dwvOldLatest = deploymentDAO.findLatestDeployment(dwvOld.deployment.getId());
        if (!Objects.equals(dwvOldLatest.deploymentVersion.getId(), dwvOld.deploymentVersion.getId())) {
            throw new InternalApplicationException("Last deployed version of process definition '" + dwvOldLatest.deployment.getName() + "' is '" +
                    dwvOldLatest.deploymentVersion.getVersion() + "'. You provided process definition id for version '" +
                    dwvOld.deploymentVersion.getVersion() + "'");
        }

        ProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(dwvOld.deployment.getName(), definition.getName())) {
            throw new DefinitionNameMismatchException(dwvOld.deployment.getName(), definition.getName());
        }
        Deployment d = definition.getDeployment();
        DeploymentVersion dv = definition.getDeploymentVersion();
        if (categories != null) {
            d.setCategories(categories);
        } else {
            d.setCategory(dwvOld.deployment.getCategory());
        }
        try {
            checkCommentsOnDeploy(parseProcessDefinition(dwvOld.deploymentVersion.getContent()), definition);
        } catch (InvalidDefinitionException e) {
            log.warn(dwvOld + ": " + e);
        }
        dv.setCreateDate(new Date());
        dv.setCreateActor(user.getActor());
        dv.setVersion(dwvOldLatest.deploymentVersion.getVersion() + 1);
        deploymentDAO.create(d);
        deploymentVersionDAO.create(dv);
        log.debug("Process definition " + dwvOld + " was successfully redeployed");
        return new WfDefinition(definition, true);
    }

    /**
     * Updates process definition (same version).
     */
    public WfDefinition updateProcessDefinition(User user, Long deploymentVersionId, @NonNull byte[] processArchiveBytes) {
        val dwv = deploymentDAO.findDeployment(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, dwv.deployment);
        val definition = parseProcessDefinition(processArchiveBytes);
        if (!Objects.equals(dwv.deployment.getName(), definition.getName())) {
            throw new DefinitionNameMismatchException(dwv.deployment.getName(), definition.getName());
        }
        checkCommentsOnDeploy(parseProcessDefinition(dwv.deploymentVersion.getContent()), definition);
        dwv.deploymentVersion.setContent(definition.getDeploymentVersion().getContent());
        dwv.deploymentVersion.setUpdateDate(new Date());
        dwv.deploymentVersion.setUpdateActor(user.getActor());
        deploymentVersionDAO.update(dwv.deploymentVersion);
        addUpdatedDefinitionInProcessLog(user, dwv);
        log.debug("Process definition " + dwv + " was successfully updated");
        return new WfDefinition(dwv);
    }

    private void checkCommentsOnDeploy(ProcessDefinition oldDefinition, ProcessDefinition definition) {
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

    public void setProcessDefinitionSubprocessBindingDate(User user, Long deploymentVersionId, Date date) {
        DeploymentWithVersion dwv = deploymentDAO.findDeployment(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, dwv.deployment);
        Date oldDate = dwv.deploymentVersion.getSubprocessBindingDate();
        dwv.deploymentVersion.setSubprocessBindingDate(date);
        deploymentVersionDAO.update(dwv.deploymentVersion);
        log.info("ProcessDefinition subprocessBindingDate changed: " + CalendarUtil.formatDateTime(oldDate) + " -> "
                + CalendarUtil.formatDateTime(date));
    }

    private void addUpdatedDefinitionInProcessLog(User user, DeploymentWithVersion dwv) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(dwv.deployment.getName());
        filter.setDefinitionVersion(dwv.deploymentVersion.getVersion());
        List<Process> processes = processDAO.getProcesses(filter);
        for (Process process : processes) {
            processLogDAO.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_CURRENT_PROCESS_VERSION), process, null);
        }
    }

    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        ProcessDefinition definition = getLatestDefinition(definitionName);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getDeployment()));
    }

    public WfDefinition getProcessDefinitionVersion(User user, String name, Long version) {
        DeploymentWithVersion dwv = deploymentDAO.findDeployment(name, version);
        ProcessDefinition definition = getDefinition(dwv.deploymentVersion.getId());
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getDeployment()));
    }

    public WfDefinition getProcessDefinition(User user, long deploymentVersionId) {
        try {
            val definition = getDefinition(deploymentVersionId);
            permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
            return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getDeployment()));
        } catch (Exception e) {
            val dwv = deploymentDAO.findDeployment(deploymentVersionId);
            permissionDAO.checkAllowed(user, Permission.LIST, dwv.deployment);
            return new WfDefinition(dwv);
        }
    }

    public ProcessDefinition getParsedProcessDefinition(User user, long deploymentVersionId) {
        ProcessDefinition pd = getDefinition(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, pd.getDeployment());
        return pd;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, long deploymentVersionId, String subprocessId) {
        ProcessDefinition definition = getDefinition(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<DeploymentWithVersion> dwvs = deploymentDAO.findAllDeploymentVersions(name);
        if (dwvs.isEmpty() || !permissionDAO.isAllowed(user, Permission.LIST, dwvs.get(0).deployment)) {
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
        List<Process> processes = processDAO.getProcesses(filter);
        for (Process process : processes) {
            NodeProcess parent = nodeProcessDAO.findBySubProcessId(process.getId());
            if (parent != null) {
                throw new ParentProcessExistsException(definitionName, parent.getProcess().getDeploymentVersion().getDeployment().getName());
            }
        }

        if (version == null) {
            Deployment latestDeployment = deploymentDAO.findLatestDeployment(definitionName);
            permissionDAO.checkAllowed(user, Permission.ALL, latestDeployment);
            permissionDAO.deleteAllPermissions(latestDeployment);
            List<Deployment> deployments = deploymentDAO.findAllDeploymentVersions(definitionName);
            for (Deployment deployment : deployments) {
                removeDeployment(user, deployment);
            }
            log.info("Process definition " + latestDeployment + " successfully undeployed");
        } else {
            DeploymentWithVersion dwv = deploymentDAO.findDeployment(definitionName, version);
            removeDeployment(user, deployment);
            log.info("Process definition " + deployment + " successfully undeployed");
        }
    }

    private void removeDeployment(User user, Deployment deployment) {
        List<Process> processes = processDAO.findAllProcesses(deployment.getId());
        for (Process process : processes) {
            deleteProcess(user, process);
        }
        deploymentVersionDAO.deleteAll(deployment);  // TODO Wrong, this is about deleting SINGLE version!!!
        deploymentDAO.delete(deployment);
        systemLogDAO.create(new ProcessDefinitionDeleteLog(user.getActor().getId(), deployment.getName(), deployment.getVersion()));
    }

    public List<ProcessDefinitionChange> getChanges(long deploymentVersionId) {
        List<Long> deploymentVersionIds = deploymentDAO.findAllDeploymentVersionIds(deploymentVersionId, true);
        return getChanges(deploymentVersionIds);
    }

    public List<ProcessDefinitionChange> getLastChanges(long deploymentVersionId, int n) {
        Preconditions.checkArgument(n > 0);
        List<Long> deploymentVersionIds = deploymentDAO.findAllDeploymentVersionIds(deploymentVersionId, false);
        if (n < deploymentVersionIds.size()) {
            deploymentVersionIds = new ArrayList<>(deploymentVersionIds.subList(0, n));
        }
        Collections.reverse(deploymentVersionIds);
        return getChanges(deploymentVersionIds);
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<Long> deploymentVersionIds = deploymentDAO.findDeploymentVersionIds(definitionName, version1, version2);
        return getChanges(deploymentVersionIds);
    }

    public byte[] getFile(User user, long deploymentVersionId, String fileName) {
        DeploymentWithVersion dwv = deploymentDAO.findDeployment(deploymentVersionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName) && !fileName.endsWith(IFileDataProvider.BOTS_XML_FILE)) {
            permissionDAO.checkAllowed(user, Permission.LIST, dwv.deployment);
        }
        if (IFileDataProvider.PAR_FILE.equals(fileName)) {
            return dwv.deploymentVersion.getContent();
        }
        ProcessDefinition definition = getDefinition(deploymentVersionId);
        return definition.getFileData(fileName);
    }

    public byte[] getGraph(User user, long deploymentVersionId, String subprocessId) {
        ProcessDefinition definition = getDefinition(deploymentVersionId);
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        return definition.getGraphImageBytesNotNull();
    }

    public Interaction getStartInteraction(User user, long deploymentVersionId) {
        ProcessDefinition definition = getDefinition(deploymentVersionId);
        Interaction interaction = definition.getInteractionNotNull(definition.getStartStateNotNull().getNodeId());
        val defaultValues = definition.getDefaultVariableValues();
        for (val entry : defaultValues.entrySet()) {
            interaction.getDefaultVariableValues().put(entry.getKey(), entry.getValue());
        }
        return interaction;
    }

    public List<SwimlaneDefinition> getSwimlanes(User user, long deploymentVersionId) {
        ProcessDefinition definition = processDefinitionLoader.getDefinition(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, long deploymentVersionId) {
        ProcessDefinition definition = getDefinition(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, long deploymentVersionId, String variableName) {
        ProcessDefinition definition = getDefinition(deploymentVersionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return definition.getVariable(variableName, true);
    }

    public List<WfDefinition> getProcessDefinitions(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        CompilerParameters parameters = CompilerParameters.create(enablePaging).loadOnlyIdentity();
        return getProcessDefinitions(user, batchPresentation, parameters);
    }

    public int getProcessDefinitionsCount(User user, BatchPresentation batchPresentation) {
        CompilerParameters parameters = CompilerParameters.createNonPaged();
        return new PresentationCompiler<Deployment>(batchPresentation).getCount(parameters);
    }

    public List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        List<String> processNameRestriction = getProcessNameRestriction(user);
        if (processNameRestriction.isEmpty()) {
            return Lists.newArrayList();
        }
        CompilerParameters parameters = CompilerParameters.create(enablePaging).addOwners(new RestrictionsToOwners(processNameRestriction, "name"));
        List<Deployment> deployments = new PresentationCompiler<Deployment>(batchPresentation).getBatch(parameters);
        List<WfDefinition> definitions = Lists.newArrayList();
        for (Deployment deployment : deployments) {
            definitions.add(new WfDefinition(deployment));
        }
        return definitions;
    }

    private ProcessDefinition parseProcessDefinition(byte[] data) {
        try {
            val d = new Deployment();
            val dv = new DeploymentVersion();
            dv.setDeployment(d);
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
        final Map<Deployment, ProcessDefinition> processDefinitions = Maps.newHashMapWithExpectedSize(deploymentIds.size());
        List<Deployment> deployments = Lists.newArrayListWithExpectedSize(deploymentIds.size());
        for (Number definitionId : deploymentIds) {
            try {
                ProcessDefinition definition = getDefinition(definitionId.longValue());
                processDefinitions.put(definition.getDeployment(), definition);
                deployments.add(definition.getDeployment());
            } catch (Exception e) {
                Deployment deployment = deploymentDAO.get(definitionId.longValue());
                if (deployment != null) {
                    processDefinitions.put(deployment, null);
                    deployments.add(deployment);
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
        for (String deploymentName : deploymentDAO.findDeploymentNames()) {
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

    private List<ProcessDefinitionChange> getChanges(List<Long> deploymentVersionIds) {
        List<ProcessDefinitionChange> ignoredChanges = null;
        if (!deploymentVersionIds.isEmpty()) {
            ProcessDefinition firstDefinition = getDefinition(deploymentVersionIds.get(0));
            long firstDeploymentVersion = firstDefinition.getDeploymentVersion().getVersion();
            Long previousDeploymentVersionId = deploymentDAO.findDeploymentVersionIdLatestVersionLessThan(
                    firstDefinition.getDeployment().getId(),
                    firstDeploymentVersion
            );
            if (previousDeploymentVersionId != null) {
                ignoredChanges = getDefinition(previousDeploymentVersionId).getChanges();
            }
        }
        val result = new ArrayList<ProcessDefinitionChange>();
        for (val deploymentVersionId : deploymentVersionIds) {
            ProcessDefinition processDefinition = getDefinition(deploymentVersionId);
            for (ProcessDefinitionChange change : processDefinition.getChanges()) {
                if (ignoredChanges != null && ignoredChanges.contains(change) || result.contains(change)) {
                    continue;
                }
                result.add(new ProcessDefinitionChange(processDefinition.getDeploymentVersion().getVersion(), change));
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
        private final Map<Deployment, ProcessDefinition> processDefinitions;

        private StartProcessPermissionCheckCallback(List<WfDefinition> result, Map<Deployment, ProcessDefinition> processDefinitions) {
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
            ProcessDefinition definition = processDefinitions.get(securedObject);
            if (definition != null) {
                result.add(new WfDefinition(definition, canBeStarted));
            } else {
                result.add(new WfDefinition((Deployment) securedObject));
            }
        }
    }
}
