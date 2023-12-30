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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CheckMassPermissionCallback;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentWithContent;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.definition.update.ProcessDefinitionUpdateManager;
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
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Created on 15.03.2005
 */
public class DefinitionLogic extends WfCommonLogic {
    @Autowired
    ProcessDefinitionUpdateManager processDefinitionUpdateManager;

    public WfDefinition deployProcessDefinition(User user, byte[] processArchiveBytes, List<String> categories) {
        permissionDao.checkAllowed(user, Permission.CREATE_DEFINITION, SecuredSingleton.SYSTEM);
        ProcessDefinition definition;
        try {
            definition = parseProcessDefinition(processArchiveBytes);
        } catch (Exception e) {
            throw new DefinitionArchiveFormatException(e);
        }
        try {
            getLatestDefinition(definition.getName());
            throw new DefinitionAlreadyExistException(definition.getName());
        } catch (DefinitionDoesNotExistException e) {
            // expected
        }
        definition.getDeployment().setCategories(categories);
        definition.getDeployment().setCreateDate(new Date());
        definition.getDeployment().setCreateActor(user.getActor());
        DeploymentWithContent deploymentWithContent = new DeploymentWithContent(definition.getDeployment(), processArchiveBytes);
        deploymentWithContentDao.deploy(deploymentWithContent, null);
        permissionDao.setPermissions(user.getActor(), ApplicablePermissions.listVisible(SecuredObjectType.DEFINITION), definition.getDeployment());
        log.debug("Deployed process definition " + definition);
        return new WfDefinition(deploymentWithContent);
    }

    public WfDefinition redeployProcessDefinition(User user, Long definitionId, byte[] processArchiveBytes, List<String> categories) {
        Deployment oldDeployment = deploymentDao.getNotNull(definitionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, oldDeployment);
        if (processArchiveBytes == null) {
            Preconditions.checkNotNull(categories, "In mode 'update only categories' categories are required");
            oldDeployment.setCategories(categories);
            return getProcessDefinition(user, definitionId);
        }
        ProcessDefinition definition;
        try {
            definition = parseProcessDefinition(processArchiveBytes);
        } catch (Exception e) {
            throw new DefinitionArchiveFormatException(e);
        }
        if (!oldDeployment.getName().equals(definition.getName())) {
            throw new DefinitionNameMismatchException("Expected definition name " + oldDeployment.getName(), definition.getName(),
                    oldDeployment.getName());
        }
        if (categories != null) {
            definition.getDeployment().setCategories(categories);
        } else {
            definition.getDeployment().setCategory(oldDeployment.getCategory());
        }
        try {
            ProcessDefinition oldDefinition = processDefinitionLoader.getDefinition(oldDeployment.getId());
            boolean containsAllPreviousComments = definition.getChanges().containsAll(oldDefinition.getChanges());
            if (!SystemProperties.isDefinitionDeploymentWithCommentsCollisionsAllowed()) {
                if (!containsAllPreviousComments) {
                    throw new InternalApplicationException("The new version of definition must contain all version comments which exists in earlier "
                            + "uploaded definition. Most likely you try to upload an old version of definition (page update is recommended).");
                }
            }
            if (!SystemProperties.isDefinitionDeploymentWithEmptyCommentsAllowed()) {
                if (containsAllPreviousComments && definition.getChanges().size() == oldDefinition.getChanges().size()) {
                    throw new InternalApplicationException("The new version of definition must contain more than "
                            + oldDefinition.getChanges().size() + " version comments. Uploaded definition contains " + definition.getChanges().size()
                            + " comments. Most likely you try to upload an old version of definition (page update is recommended). ");
                }
            }
        } catch (InvalidDefinitionException e) {
            log.warn(oldDeployment + ": " + e);
        }
        definition.getDeployment().setCreateDate(new Date());
        definition.getDeployment().setCreateActor(user.getActor());
        DeploymentWithContent deploymentWithContent = new DeploymentWithContent(definition.getDeployment(), processArchiveBytes);
        deploymentWithContentDao.deploy(deploymentWithContent, oldDeployment);
        log.debug("Process definition " + oldDeployment + " was successfully redeployed");
        return new WfDefinition(deploymentWithContent);
    }

    /**
     * Updates process definition.
     */
    public WfDefinition updateProcessDefinition(User user, Long definitionId, byte[] processArchiveBytes) {
        Preconditions.checkNotNull(processArchiveBytes, "processArchiveBytes is required!");
        DeploymentWithContent deploymentWithContent = deploymentWithContentDao.getNotNull(definitionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, deploymentWithContent);
        ProcessDefinition uploadedDefinition;
        try {
            uploadedDefinition = parseProcessDefinition(processArchiveBytes);
        } catch (Exception e) {
            throw new DefinitionArchiveFormatException(e);
        }
        if (!deploymentWithContent.getName().equals(uploadedDefinition.getName())) {
            throw new DefinitionNameMismatchException("Expected definition name " + deploymentWithContent.getName(), uploadedDefinition.getName(),
                    deploymentWithContent.getName());
        }
        ProcessDefinition oldDefinition = getDefinition(deploymentWithContent.getId());
        boolean containsAllPreviousComments = uploadedDefinition.getChanges().containsAll(oldDefinition.getChanges());
        if (!SystemProperties.isDefinitionDeploymentWithCommentsCollisionsAllowed()) {
            if (!containsAllPreviousComments) {
                throw new InternalApplicationException("The new version of definition must contain all version comments which exists in earlier "
                        + "uploaded definition. Most likely you try to upload an old version of definition (page update is recommended).");
            }
        }
        if (!SystemProperties.isDefinitionDeploymentWithEmptyCommentsAllowed()) {
            if (containsAllPreviousComments && uploadedDefinition.getChanges().size() == oldDefinition.getChanges().size()) {
                throw new InternalApplicationException("The new version of definition must contain more than " + oldDefinition.getChanges().size()
                        + " version comments. Uploaded definition contains " + uploadedDefinition.getChanges().size()
                        + " comments. Most likely you try to upload an old version of definition (page update is recommended). ");
            }
        }
        List<Process> processes = processDefinitionUpdateManager.findApplicableProcesses(oldDefinition);
        Set<Process> affectedProcesses = processDefinitionUpdateManager.before(oldDefinition, uploadedDefinition, processes);
        deploymentWithContent.setContent(processArchiveBytes);
        deploymentWithContent.setUpdateDate(new Date());
        deploymentWithContent.setUpdateActor(user.getActor());
        deploymentWithContentDao.update(deploymentWithContent);
        addUpdatedDefinitionInProcessLog(user, deploymentWithContent.getName(), deploymentWithContent.getVersion());
        processDefinitionUpdateManager.after(uploadedDefinition, affectedProcesses);
        log.debug("Process definition " + deploymentWithContent + " was successfully updated");
        return new WfDefinition(deploymentWithContent);
    }

    public void setProcessDefinitionSubprocessBindingDate(User user, Long definitionId, Date date) {
        Deployment deployment = deploymentDao.getNotNull(definitionId);
        permissionDao.checkAllowed(user, Permission.UPDATE, deployment);
        Date oldDate = deployment.getSubprocessBindingDate();
        deployment.setSubprocessBindingDate(date);
        deploymentDao.update(deployment);
        log.info("ProcessDefinition subprocessBindingDate changed: " + CalendarUtil.formatDateTime(oldDate) + " -> "
                + CalendarUtil.formatDateTime(date));
    }

    private void addUpdatedDefinitionInProcessLog(User user, String definitionName, Long definitionVersion) {
        ProcessFilter filter = new ProcessFilter();
        filter.setDefinitionName(definitionName);
        filter.setDefinitionVersion(definitionVersion);
        filter.setFinished(false);
        List<Process> processes = processDao.getProcesses(filter);
        for (Process process : processes) {
            processLogDao.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPGRADE_CURRENT_PROCESS_VERSION, null), process, null);
        }
    }

    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        ProcessDefinition definition = getLatestDefinition(definitionName);
        permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
        return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START_PROCESS, definition.getDeployment()));
    }

    public WfDefinition getProcessDefinitionVersion(User user, String name, Long version) {
        Deployment deployment = deploymentDao.findDeployment(name, version);
        ProcessDefinition definition = getDefinition(deployment.getId());
        permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
        return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START_PROCESS, definition.getDeployment()));
    }

    public WfDefinition getProcessDefinition(User user, Long definitionId) {
        try {
            ProcessDefinition definition = getDefinition(definitionId);
            permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
            return new WfDefinition(definition, permissionDao.isAllowed(user, Permission.START_PROCESS, definition.getDeployment()));
        } catch (Exception e) {
            Deployment deployment = deploymentDao.getNotNull(definitionId);
            permissionDao.checkAllowed(user, Permission.READ, deployment);
            return new WfDefinition(deployment);
        }
    }

    public ProcessDefinition getParsedProcessDefinition(User user, Long definitionId) {
        ProcessDefinition processDefinition = getDefinition(definitionId);
        permissionDao.checkAllowed(user, Permission.READ, processDefinition.getDeployment());
        return processDefinition;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long definitionId, String subprocessId) {
        ProcessDefinition definition = getDefinition(definitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(user, definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<Deployment> deploymentVersions = deploymentDao.findAllDeploymentVersions(name);
        final List<WfDefinition> result = Lists.newArrayListWithExpectedSize(deploymentVersions.size());
        isPermissionAllowed(user, deploymentVersions, Permission.READ, new CheckMassPermissionCallback() {
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
        List<Process> processes = processDao.getProcesses(filter);
        for (Process process : processes) {
            if (nodeProcessDao.findBySubProcessId(process.getId()) != null) {
                throw new ParentProcessExistsException(definitionName, nodeProcessDao.findBySubProcessId(process.getId()).getProcess()
                        .getDeployment().getName());
            }
        }
        if (version == null) {
            Deployment latestDeployment = deploymentDao.findLatestDeployment(definitionName);
            permissionDao.checkAllowed(user, Permission.DELETE, latestDeployment);
            permissionDao.deleteAllPermissions(latestDeployment);
            List<Deployment> deployments = deploymentDao.findAllDeploymentVersions(definitionName);
            for (Deployment deployment : deployments) {
                removeDeployment(user, deployment);
            }
            log.info("Process definition " + latestDeployment + " successfully undeployed");
        } else {
            Deployment deployment = deploymentDao.findDeployment(definitionName, version);
            removeDeployment(user, deployment);
            log.info("Process definition " + deployment + " successfully undeployed");
        }
    }

    private void removeDeployment(User user, Deployment deployment) {
        List<Process> processes = processDao.findAllProcesses(deployment.getId());
        for (Process process : processes) {
            deleteProcess(user, process);
        }
        deploymentDao.delete(deployment);
        systemLogDao.create(new ProcessDefinitionDeleteLog(user.getActor().getId(), deployment.getName(), deployment.getVersion()));
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<Long> deploymentIds = deploymentDao.findDeploymentVersionIds(definitionName, version1, version2);
        return getChanges(deploymentIds);
    }

    public byte[] getFile(User user, Long definitionId, String fileName) {
        ProcessDefinition definition = getDefinition(definitionId);
        Deployment deployment = definition.getDeployment();
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName) && !fileName.endsWith(FileDataProvider.BOTS_XML_FILE)) {
            permissionDao.checkAllowed(user, Permission.READ, deployment);
        }
        if (FileDataProvider.PAR_FILE.equals(fileName)) {
            return deploymentWithContentDao.get(deployment.getId()).getContent();
        }
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
        permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, Long definitionId) {
        ProcessDefinition definition = getDefinition(definitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, Long definitionId, String variableName) {
        ProcessDefinition definition = getDefinition(definitionId);
        permissionDao.checkAllowed(user, Permission.READ, definition.getDeployment());
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
        ProcessArchive archive = new ProcessArchive(new Deployment(), data);
        return archive.parseProcessDefinition();
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
                Deployment deployment = deploymentDao.get(definitionId.longValue());
                if (deployment != null) {
                    processDefinitions.put(deployment, null);
                    deployments.add(deployment);
                }
            }
        }
        final List<WfDefinition> result = Lists.newArrayListWithExpectedSize(deploymentIds.size());
        isPermissionAllowed(user, deployments, Permission.START_PROCESS,
                new StartProcessPermissionCheckCallback(result, processDefinitions));
        return result;
    }

    private List<String> getProcessNameRestriction(User user) {
        List<ru.runa.wfe.definition.logic.DefinitionLogic.DefinitionSecuredObject> definitionSecuredObjects = new ArrayList<>();
        for (String deploymentName : deploymentDao.findDeploymentNames()) {
            definitionSecuredObjects.add(new ru.runa.wfe.definition.logic.DefinitionLogic.DefinitionSecuredObject(deploymentName));
        }
        final List<String> definitionsWithPermission = new ArrayList<>();
        isPermissionAllowed(user, definitionSecuredObjects, Permission.READ, new CheckMassPermissionCallback() {
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
            Long previousDefinitionId = deploymentDao.findDeploymentIdLatestVersionLessThan(firstDefinition.getName(), firstDeploymentVersion);
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
