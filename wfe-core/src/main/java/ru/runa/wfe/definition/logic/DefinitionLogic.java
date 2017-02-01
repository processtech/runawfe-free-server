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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CheckMassPermissionCallback;
import ru.runa.wfe.commons.logic.IgnoreDeniedPermissionCallback;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.VersionInfo;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.par.CommentsParser;
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
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created on 15.03.2005
 */
public class DefinitionLogic extends WFCommonLogic {

    static final SecuredObjectType[] securedObjectTypes = new SecuredObjectType[] { SecuredObjectType.DEFINITION };

    public WfDefinition deployProcessDefinition(User user, byte[] processArchiveBytes, List<String> categories) {
        checkPermissionAllowed(user, ASystem.INSTANCE, WorkflowSystemPermission.DEPLOY_DEFINITION);
        ProcessDefinition definition = null;
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
        deploymentDAO.deploy(definition.getDeployment(), null);
        Collection<Permission> allPermissions = new DefinitionPermission().getAllPermissions();
        permissionDAO.setPermissions(user.getActor(), allPermissions, definition.getDeployment());
        log.debug("Deployed process definition " + definition);
        return new WfDefinition(definition, true);
    }

    public WfDefinition redeployProcessDefinition(User user, Long definitionId, byte[] processArchiveBytes, List<String> categories) {
        Deployment oldDeployment = deploymentDAO.getNotNull(definitionId);
        checkPermissionAllowed(user, oldDeployment, DefinitionPermission.REDEPLOY_DEFINITION);
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
        ProcessDefinition oldDefinition = parseProcessDefinition(oldDeployment.getContent());
        boolean containsAllPreviousComments = definition.getVersionInfoList().containsAll(oldDefinition.getVersionInfoList());
        if (!SystemProperties.isDefinitionDeploymentWithCommentsCollisionsAllowed()) {
            if (containsAllPreviousComments != true) {
                throw new InternalApplicationException("The new version of definition must contains all version comments which exists in earlier "
                        + "uploaded definition. Most likely you try to upload an old version of definition (page update is recommended).");
            }
        }
        if (!SystemProperties.isDefinitionDeploymentWithEmptyCommentsAllowed()) {
            if (containsAllPreviousComments && definition.getVersionInfoList().size() == oldDefinition.getVersionInfoList().size()) {
                throw new InternalApplicationException("The new version of definition must contains more than "
                        + oldDefinition.getVersionInfoList().size() + " version comments. Uploaded definition contains "
                        + definition.getVersionInfoList().size()
                        + " comments. Most likely you try to upload an old version of definition (page update is recommended). ");
            }
        }
        definition.getDeployment().setCreateDate(new Date());
        definition.getDeployment().setCreateActor(user.getActor());
        deploymentDAO.deploy(definition.getDeployment(), oldDeployment);
        log.debug("Process definition " + oldDeployment + " was successfully redeployed");
        return new WfDefinition(definition, true);
    }

    /**
     * Updates process definition.
     * 
     * @param user
     * @param definitionId
     * @param processArchiveBytes
     * @return
     */
    public WfDefinition updateProcessDefinition(User user, Long definitionId, byte[] processArchiveBytes) {
        Preconditions.checkNotNull(processArchiveBytes, "processArchiveBytes is required!");
        Deployment deployment = deploymentDAO.getNotNull(definitionId);
        checkPermissionAllowed(user, deployment, DefinitionPermission.REDEPLOY_DEFINITION);
        ProcessDefinition uploadedDefinition;
        try {
            uploadedDefinition = parseProcessDefinition(processArchiveBytes);
        } catch (Exception e) {
            throw new DefinitionArchiveFormatException(e);
        }
        if (!deployment.getName().equals(uploadedDefinition.getName())) {
            throw new DefinitionNameMismatchException("Expected definition name " + deployment.getName(), uploadedDefinition.getName(),
                    deployment.getName());
        }
        ProcessDefinition oldDefinition = parseProcessDefinition(deployment.getContent());
        boolean containsAllPreviousComments = uploadedDefinition.getVersionInfoList().containsAll(oldDefinition.getVersionInfoList());
        if (!SystemProperties.isDefinitionDeploymentWithCommentsCollisionsAllowed()) {
            if (containsAllPreviousComments != true) {
                throw new InternalApplicationException("The new version of definition must contains all version comments which exists in earlier "
                        + "uploaded definition. Most likely you try to upload an old version of definition (page update is recommended).");
            }
        }
        if (!SystemProperties.isDefinitionDeploymentWithEmptyCommentsAllowed()) {
            if (containsAllPreviousComments && uploadedDefinition.getVersionInfoList().size() == oldDefinition.getVersionInfoList().size()) {
                throw new InternalApplicationException("The new version of definition must contains more than "
                        + oldDefinition.getVersionInfoList().size() + " version comments. Uploaded definition contains "
                        + uploadedDefinition.getVersionInfoList().size()
                        + " comments. Most likely you try to upload an old version of definition (page update is recommended). ");
            }
        }
        deployment.setContent(uploadedDefinition.getDeployment().getContent());
        deployment.setUpdateDate(new Date());
        deployment.setUpdateActor(user.getActor());
        deploymentDAO.update(deployment);
        addUpdatedDefinitionInProcessLog(user, deployment);
        log.debug("Process definition " + deployment + " was successfully updated");
        return new WfDefinition(deployment);
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
        checkPermissionAllowed(user, definition.getDeployment(), Permission.READ);
        return new WfDefinition(definition, isPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.START_PROCESS));
    }

    public WfDefinition getProcessDefinitionVersion(User user, String name, Long version) {
        Deployment deployment = deploymentDAO.findDeployment(name, version);
        ProcessDefinition definition = getDefinition(deployment.getId());
        checkPermissionAllowed(user, definition.getDeployment(), Permission.READ);
        return new WfDefinition(definition, isPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.START_PROCESS));
    }

    public WfDefinition getProcessDefinition(User user, Long definitionId) {
        try {
            ProcessDefinition definition = getDefinition(definitionId);
            checkPermissionAllowed(user, definition.getDeployment(), Permission.READ);
            return new WfDefinition(definition, isPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.START_PROCESS));
        } catch (Exception e) {
            Deployment deployment = deploymentDAO.getNotNull(definitionId);
            checkPermissionAllowed(user, deployment, Permission.READ);
            return new WfDefinition(deployment);
        }
    }

    public ProcessDefinition getParsedProcessDefinition(User user, Long definitionId) {
        ProcessDefinition processDefinition = getDefinition(definitionId);
        checkPermissionAllowed(user, processDefinition.getDeployment(), Permission.READ);
        return processDefinition;
    }

    public List<NodeGraphElement> getProcessDefinitionGraphElements(User user, Long definitionId, String subprocessId) {
        ProcessDefinition definition = getDefinition(definitionId);
        checkPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.READ);
        if (subprocessId != null) {
            definition = definition.getEmbeddedSubprocessByIdNotNull(subprocessId);
        }
        ProcessDefinitionInfoVisitor visitor = new ProcessDefinitionInfoVisitor(user, definition, processDefinitionLoader);
        return getDefinitionGraphElements(user, definition, visitor);
    }

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        List<Deployment> deploymentVersions = deploymentDAO.findAllDeploymentVersions(name);
        final List<WfDefinition> result = Lists.newArrayListWithExpectedSize(deploymentVersions.size());
        isPermissionAllowed(user, deploymentVersions, Permission.READ, new IgnoreDeniedPermissionCallback() {
            @Override
            public void OnPermissionGranted(Identifiable identifiable) {
                result.add(new WfDefinition((Deployment) identifiable));
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
                throw new ParentProcessExistsException(definitionName, nodeProcessDAO.findBySubProcessId(process.getId()).getProcess()
                        .getDeployment().getName());
            }
        }
        if (version == null) {
            Deployment latestDeployment = deploymentDAO.findLatestDeployment(definitionName);
            checkPermissionAllowed(user, latestDeployment, DefinitionPermission.UNDEPLOY_DEFINITION);
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
        List<ProcessDefinitionChange> result = new ArrayList<>();
        String definitionName = deploymentDAO.get(definitionId).getName();
        List<Deployment> listOfDeployments = deploymentDAO.findAllDeploymentVersions(definitionName);
        int previousCount = 0;
        for (int m = listOfDeployments.size() - 1; m >= 0; m--) {
            Deployment deployment = listOfDeployments.get(m);
            int currentVersion = deployment.getVersion().intValue();
            String fileName = IFileDataProvider.COMMENTS_XML_FILE_NAME;
            ProcessArchive archiveData = new ProcessArchive(deployment);
            if (archiveData.getFileData().containsKey(fileName)) {
                byte[] definitionXml = archiveData.getFileData().get(fileName);
                Document document = XmlUtils.parseWithoutValidation(definitionXml);
                List<Element> versionList = document.getRootElement().elements(CommentsParser.VERSION);
                List<VersionInfo> versionInfos = Lists.newArrayList();
                for (int j = previousCount; j < versionList.size(); j++) {
                    Element versionInfoElement = versionList.get(j);
                    VersionInfo versionInfo = new VersionInfo();
                    versionInfo.setDateTime(versionInfoElement.elementText(CommentsParser.VERSION_DATE));
                    versionInfo.setAuthor(versionInfoElement.elementText(CommentsParser.VERSION_AUTHOR));
                    versionInfo.setComment(versionInfoElement.elementText(CommentsParser.VERSION_COMMENT));
                    versionInfos.add(versionInfo);
                    previousCount++;
                }

                for (VersionInfo versionInfo : versionInfos) {
                    result.add(new ProcessDefinitionChange(currentVersion, versionInfo));
                }
            }

        }
        return result;
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        List<ProcessDefinitionChange> result = new ArrayList<>();
        List<Deployment> listOfDeployments = deploymentDAO.findAllDeploymentVersions(definitionName);
        int previousCount = 0;
        for (int m = listOfDeployments.size() - 1; m >= 0; m--) {
            Deployment deployment = listOfDeployments.get(m);
            int currentVersion = deployment.getVersion().intValue();
            String fileName = IFileDataProvider.COMMENTS_XML_FILE_NAME;
            ProcessArchive archiveData = new ProcessArchive(deployment);
            if (archiveData.getFileData().containsKey(fileName)) {
                byte[] definitionXml = archiveData.getFileData().get(fileName);
                Document document = XmlUtils.parseWithoutValidation(definitionXml);
                List<Element> versionList = document.getRootElement().elements(CommentsParser.VERSION);
                List<VersionInfo> versionInfos = Lists.newArrayList();
                for (int j = previousCount; j < versionList.size(); j++) {
                    Element versionInfoElement = versionList.get(j);
                    VersionInfo versionInfo = new VersionInfo();
                    versionInfo.setDateTime(versionInfoElement.elementText(CommentsParser.VERSION_DATE));
                    versionInfo.setAuthor(versionInfoElement.elementText(CommentsParser.VERSION_AUTHOR));
                    versionInfo.setComment(versionInfoElement.elementText(CommentsParser.VERSION_COMMENT));
                    versionInfos.add(versionInfo);
                    previousCount++;
                }

                if (currentVersion >= version1 && currentVersion <= version2) {
                    for (VersionInfo versionInfo : versionInfos) {
                        result.add(new ProcessDefinitionChange(currentVersion, versionInfo));
                    }
                }
            }
        }
        return result;
    }

    public List<ProcessDefinitionChange> findChanges(Date date1, Date date2) {
        List<ProcessDefinitionChange> result = new ArrayList<>();
        List<Deployment> listOfDeployments = deploymentDAO.getAll();
        int previousCount = 0;
        for (int m = listOfDeployments.size() - 1; m >= 0; m--) {
            Deployment deployment = listOfDeployments.get(m);
            int currentVersion = deployment.getVersion().intValue();
            String fileName = IFileDataProvider.COMMENTS_XML_FILE_NAME;
            ProcessArchive archiveData = new ProcessArchive(deployment);
            if (archiveData.getFileData().containsKey(fileName)) {
                byte[] definitionXml = archiveData.getFileData().get(fileName);
                Document document = XmlUtils.parseWithoutValidation(definitionXml);
                List<Element> versionList = document.getRootElement().elements(CommentsParser.VERSION);
                List<VersionInfo> versionInfos = Lists.newArrayList();
                for (int j = previousCount; j < versionList.size(); j++) {
                    Element versionInfoElement = versionList.get(j);
                    VersionInfo versionInfo = new VersionInfo();
                    versionInfo.setDateTime(versionInfoElement.elementText(CommentsParser.VERSION_DATE));
                    versionInfo.setAuthor(versionInfoElement.elementText(CommentsParser.VERSION_AUTHOR));
                    versionInfo.setComment(versionInfoElement.elementText(CommentsParser.VERSION_COMMENT));
                    versionInfos.add(versionInfo);
                    previousCount++;
                }

                for (VersionInfo versionInfo : versionInfos) {
                    if (versionInfo.getDate().compareTo(CalendarUtil.dateToCalendar(date1)) >= 0
                            && versionInfo.getDate().compareTo(CalendarUtil.dateToCalendar(date2)) <= 0) {
                        result.add(new ProcessDefinitionChange(currentVersion, versionInfo));
                    }
                }
            }
        }
        return result;
    }

    public byte[] getFile(User user, Long definitionId, String fileName) {
        Deployment deployment = deploymentDAO.getNotNull(definitionId);
        if (!ProcessArchive.UNSECURED_FILE_NAMES.contains(fileName)) {
            checkPermissionAllowed(user, deployment, DefinitionPermission.READ);
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
        checkPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.READ);
        return definition.getSwimlanes();
    }

    public List<VariableDefinition> getProcessDefinitionVariables(User user, Long definitionId) {
        ProcessDefinition definition = getDefinition(definitionId);
        checkPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.READ);
        return definition.getVariables();
    }

    public VariableDefinition getProcessDefinitionVariable(User user, Long definitionId, String variableName) {
        ProcessDefinition definition = getDefinition(definitionId);
        checkPermissionAllowed(user, definition.getDeployment(), DefinitionPermission.READ);
        return definition.getVariable(variableName, true);
    }

    private ProcessDefinition parseProcessDefinition(byte[] data) {
        Deployment deployment = new Deployment();
        deployment.setContent(data);
        ProcessArchive archive = new ProcessArchive(deployment);
        return archive.parseProcessDefinition();
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
        isPermissionAllowed(user, deployments, DefinitionPermission.START_PROCESS,
                new StartProcessPermissionCheckCallback(result, processDefinitions));
        return result;
    }

    private List<String> getProcessNameRestriction(User user) {
        List<DefinitionIdentifiable> definitionIdentifiables = new ArrayList<DefinitionLogic.DefinitionIdentifiable>();
        for (String deploymentName : deploymentDAO.findDeploymentNames()) {
            definitionIdentifiables.add(new DefinitionIdentifiable(deploymentName));
        }
        final List<String> definitionsWithPermission = new ArrayList<String>();
        isPermissionAllowed(user, definitionIdentifiables, Permission.READ, new IgnoreDeniedPermissionCallback() {
            @Override
            public void OnPermissionGranted(Identifiable identifiable) {
                definitionsWithPermission.add(((DefinitionIdentifiable) identifiable).getDeploymentName());
            }
        });
        return definitionsWithPermission;
    }

    private final class DefinitionIdentifiable extends Identifiable {

        private static final long serialVersionUID = 1L;
        private final String deploymentName;

        public DefinitionIdentifiable(String deploymentName) {
            super();
            this.deploymentName = deploymentName;
        }

        @Override
        public Long getIdentifiableId() {
            return new Long(deploymentName.hashCode());
        }

        @Override
        public SecuredObjectType getSecuredObjectType() {
            return SecuredObjectType.DEFINITION;
        }

        public String getDeploymentName() {
            return deploymentName;
        }
    }

    private final class StartProcessPermissionCheckCallback implements CheckMassPermissionCallback {
        private final List<WfDefinition> result;
        private final Map<Deployment, ProcessDefinition> processDefinitions;

        private StartProcessPermissionCheckCallback(List<WfDefinition> result, Map<Deployment, ProcessDefinition> processDefinitions) {
            this.result = result;
            this.processDefinitions = processDefinitions;
        }

        @Override
        public void OnPermissionGranted(Identifiable identifiable) {
            addDefinitionToResult(identifiable, true);
        }

        @Override
        public void OnPermissionDenied(Identifiable identifiable) {
            addDefinitionToResult(identifiable, false);
        }

        private void addDefinitionToResult(Identifiable identifiable, boolean canBeStarted) {
            ProcessDefinition definition = processDefinitions.get(identifiable);
            if (definition != null) {
                result.add(new WfDefinition(definition, canBeStarted));
            } else {
                result.add(new WfDefinition((Deployment) identifiable));
            }
        }
    }
}
