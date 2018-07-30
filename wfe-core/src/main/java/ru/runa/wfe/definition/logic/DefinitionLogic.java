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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
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
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.par.ProcessArchive;
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

/**
 * Created on 15.03.2005
 */
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
        Deployment deployment = definition.getDeployment();
        deployment.setCategories(categories);
        deployment.setCreateDate(new Date());
        deployment.setCreateActor(user.getActor());
        deployment.setVersion(1L);
        deploymentDAO.create(deployment);
        permissionDAO.setPermissions(user.getActor(), Collections.singletonList(Permission.ALL), deployment);
        log.debug("Deployed process definition " + definition);
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, deployment));
    }

    /**
     * Adds new definition version.
     */
    public WfDefinition redeployProcessDefinition(User user, Long definitionId, byte[] processArchiveBytes, List<String> categories) {
        Deployment oldDeployment = deploymentDAO.getNotNull(definitionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, oldDeployment);
        if (processArchiveBytes == null) {
            Preconditions.checkNotNull(categories, "In mode 'update only categories' categories are required");
            oldDeployment.setCategories(categories);
            return getProcessDefinition(user, definitionId);
        }

        // TODO If categories can be changed only for latest version, move this check up, above "if (processArchiveBytes == null)".
        Deployment oldLatestDeployment = deploymentDAO.findLatestDeployment(oldDeployment.getName());
        if (!Objects.equal(oldLatestDeployment.getId(), oldDeployment.getId())) {
            throw new InternalApplicationException("Last deployed version of process definition '" + oldLatestDeployment.getName() + "' is '"
                    + oldLatestDeployment.getVersion() + "'. You provided process definition id for version '"
                    + oldDeployment.getVersion() + "'");
        }

        ProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        if (!oldDeployment.getName().equals(definition.getName())) {
            throw new DefinitionNameMismatchException("Expected definition name " + oldDeployment.getName(), definition.getName(),
                    oldDeployment.getName());
        }
        Deployment deployment = definition.getDeployment();
        if (categories != null) {
            deployment.setCategories(categories);
        } else {
            deployment.setCategory(oldDeployment.getCategory());
        }
        try {
            checkCommentsOnDeploy(parseProcessDefinition(oldDeployment.getContent()), definition);
        } catch (InvalidDefinitionException e) {
            log.warn(oldDeployment + ": " + e);
        }

        deployment.setCreateDate(new Date());
        deployment.setCreateActor(user.getActor());
        deployment.setVersion(oldLatestDeployment.getVersion() + 1);
        deploymentDAO.create(deployment);
        log.debug("Process definition " + oldDeployment + " was successfully redeployed");
        return new WfDefinition(definition, true);
    }

    /**
     * Updates process definition (same version).
     */
    public WfDefinition updateProcessDefinition(User user, Long definitionId, @NonNull byte[] processArchiveBytes) {
        Deployment deployment = deploymentDAO.getNotNull(definitionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, deployment);
        ProcessDefinition definition = parseProcessDefinition(processArchiveBytes);
        if (!deployment.getName().equals(definition.getName())) {
            throw new DefinitionNameMismatchException("Expected definition name " + deployment.getName(), definition.getName(),
                    deployment.getName());
        }
        checkCommentsOnDeploy(parseProcessDefinition(deployment.getContent()), definition);
        deployment.setContent(definition.getDeployment().getContent());
        deployment.setUpdateDate(new Date());
        deployment.setUpdateActor(user.getActor());
        deploymentDAO.update(deployment);
        addUpdatedDefinitionInProcessLog(user, deployment);
        log.debug("Process definition " + deployment + " was successfully updated");
        return new WfDefinition(deployment);
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

    public void setProcessDefinitionSubprocessBindingDate(User user, Long definitionId, Date date) {
        Deployment deployment = deploymentDAO.getNotNull(definitionId);
        permissionDAO.checkAllowed(user, Permission.UPDATE, deployment);
        Date oldDate = deployment.getSubprocessBindingDate();
        deployment.setSubprocessBindingDate(date);
        deploymentDAO.update(deployment);
        log.info("ProcessDefinition subprocessBindingDate changed: " + CalendarUtil.formatDateTime(oldDate) + " -> "
                + CalendarUtil.formatDateTime(date));
    }

    private void addUpdatedDefinitionInProcessLog(User user, Deployment deployment) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(deployment.getName());
        filter.setDefinitionVersion(deployment.getVersion());
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
        Deployment deployment = deploymentDAO.findDeployment(name, version);
        ProcessDefinition definition = getDefinition(deployment.getId());
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getDeployment()));
    }

    public WfDefinition getProcessDefinition(User user, Long definitionId) {
        try {
            ProcessDefinition definition = getDefinition(definitionId);
            permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
            return new WfDefinition(definition, permissionDAO.isAllowed(user, Permission.START, definition.getDeployment()));
        } catch (Exception e) {
            Deployment deployment = deploymentDAO.getNotNull(definitionId);
            permissionDAO.checkAllowed(user, Permission.LIST, deployment);
            return new WfDefinition(deployment);
        }
    }

    public ProcessDefinition getParsedProcessDefinition(User user, Long definitionId) {
        ProcessDefinition processDefinition = getDefinition(definitionId);
        permissionDAO.checkAllowed(user, Permission.LIST, processDefinition.getDeployment());
        return processDefinition;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long definitionId, String subprocessId) {
        ProcessDefinition definition = getDefinition(definitionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(user, definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<Deployment> deploymentVersions = deploymentDAO.findAllDeploymentVersions(name);
        final List<WfDefinition> result = Lists.newArrayListWithExpectedSize(deploymentVersions.size());
        isPermissionAllowed(user, deploymentVersions, Permission.LIST, new CheckMassPermissionCallback() {
            @Override
            public void onPermissionGranted(SecuredObject securedObject) {
                result.add(new WfDefinition((Deployment) securedObject));
            }
        });
        return result;
    }

    public void undeployProcessDefinition(User user, String definitionName, Long version) {
        Preconditions.checkNotNull(definitionName, "definitionName must be specified.");
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(definitionName);
        filter.setDefinitionVersion(version);
        List<Process> processes = processDAO.getProcesses(filter);
        for (Process process : processes) {
            if (nodeProcessDAO.findBySubProcessId(process.getId()) != null) {
                throw new ParentProcessExistsException(
                        definitionName,
                        nodeProcessDAO.findBySubProcessId(process.getId()).getProcess().getDeployment().getName()
                );
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
            Deployment deployment = deploymentDAO.findDeployment(definitionName, version);
            removeDeployment(user, deployment);
            log.info("Process definition " + deployment + " successfully undeployed");
        }
    }

    private void removeDeployment(User user, Deployment deployment) {
        List<Process> processes = processDAO.findAllProcesses(deployment.getId());
        for (Process process : processes) {
            deleteProcess(user, process);
        }
        deploymentDAO.delete(deployment);
        systemLogDAO.create(new ProcessDefinitionDeleteLog(user.getActor().getId(), deployment.getName(), deployment.getVersion()));
    }

    public List<ProcessDefinitionChange> getChanges(Long definitionId) {
        String definitionName = getDefinition(definitionId).getName();
        List<Long> deploymentIds = deploymentDAO.findAllDeploymentVersionIds(definitionName, true);
        return getChanges(deploymentIds);
    }

    public List<ProcessDefinitionChange> getLastChanges(Long definitionId, Long n) {
        Preconditions.checkArgument(n > 0);
        String definitionName = getDefinition(definitionId).getName();
        List<Long> deploymentIds = deploymentDAO.findAllDeploymentVersionIds(definitionName, false);
        if (n < deploymentIds.size()) {
            deploymentIds = new ArrayList<>(deploymentIds.subList(0, n.intValue()));
        }
        Collections.reverse(deploymentIds);
        return getChanges(deploymentIds);
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<Long> deploymentIds = deploymentDAO.findDeploymentVersionIds(definitionName, version1, version2);
        return getChanges(deploymentIds);
    }

    public byte[] getFile(User user, Long definitionId, String fileName) {
        Deployment deployment = deploymentDAO.getNotNull(definitionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName) && !fileName.endsWith(IFileDataProvider.BOTS_XML_FILE)) {
            permissionDAO.checkAllowed(user, Permission.LIST, deployment);
        }
        if (IFileDataProvider.PAR_FILE.equals(fileName)) {
            return deployment.getContent();
        }
        ProcessDefinition definition = getDefinition(definitionId);
        return definition.getFileData(fileName);
    }

    public byte[] getGraph(User user, Long definitionId, String subprocessId) {
        ProcessDefinition definition = getDefinition(definitionId);
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        return definition.getGraphImageBytesNotNull();
    }

    public Interaction getStartInteraction(User user, Long definitionId) {
        ProcessDefinition definition = getDefinition(definitionId);
        Interaction interaction = definition.getInteractionNotNull(definition.getStartStateNotNull().getNodeId());
        Map<String, Object> defaultValues = definition.getDefaultVariableValues();
        for (Map.Entry<String, Object> entry : defaultValues.entrySet()) {
            interaction.getDefaultVariableValues().put(entry.getKey(), entry.getValue());
        }
        return interaction;
    }

    public Interaction getTaskNodeInteraction(User user, Long definitionId, String nodeId) {
        ProcessDefinition definition = getDefinition(definitionId);
        return definition.getInteractionNotNull(nodeId);
    }

    public List<SwimlaneDefinition> getSwimlanes(User user, Long definitionId) {
        ProcessDefinition definition = processDefinitionLoader.getDefinition(definitionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, Long definitionId) {
        ProcessDefinition definition = getDefinition(definitionId);
        permissionDAO.checkAllowed(user, Permission.LIST, definition.getDeployment());
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, Long definitionId, String variableName) {
        ProcessDefinition definition = getDefinition(definitionId);
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
            Deployment deployment = new Deployment();
            deployment.setContent(data);
            ProcessArchive archive = new ProcessArchive(deployment);
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

    private List<ProcessDefinitionChange> getChanges(List<Long> deploymentIds) {
        List<ProcessDefinitionChange> ignoredChanges = null;
        if (!deploymentIds.isEmpty()) {
            ProcessDefinition firstDefinition = getDefinition(deploymentIds.get(0));
            Long firstDeploymentVersion = firstDefinition.getDeployment().getVersion();
            Long previousDefinitionId = deploymentDAO.findDeploymentIdLatestVersionLessThan(firstDefinition.getName(), firstDeploymentVersion);
            if (previousDefinitionId != null) {
                ignoredChanges = getDefinition(previousDefinitionId).getChanges();
            }
        }
        List<ProcessDefinitionChange> result = new ArrayList<>();
        for (Number deploymentId : deploymentIds) {
            ProcessDefinition processDefinition = getDefinition(deploymentId.longValue());
            for (ProcessDefinitionChange change : processDefinition.getChanges()) {
                if (ignoredChanges != null && ignoredChanges.contains(change) || result.contains(change)) {
                    continue;
                }
                result.add(new ProcessDefinitionChange(processDefinition.getDeployment().getVersion(), change));
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
