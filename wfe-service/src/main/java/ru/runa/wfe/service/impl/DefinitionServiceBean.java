package ru.runa.wfe.service.impl;

import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.DefinitionServiceLocal;
import ru.runa.wfe.service.decl.DefinitionServiceRemote;
import ru.runa.wfe.service.decl.DefinitionWebServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.service.jaxb.VariableConverter;
import ru.runa.wfe.springframework4.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;
import ru.runa.wfe.var.logic.VariableLogic;

@Stateless(name = "DefinitionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "DefinitionAPI", serviceName = "DefinitionWebService")
@SOAPBinding
public class DefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote, DefinitionWebServiceRemote {
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;
    @Autowired
    private VariableLogic variableLogic;

    @Override
    @WebResult(name = "result")
    public WfDefinition deployProcessDefinition(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "par") @NonNull byte[] par,
            @WebParam(name = "categories") @NonNull List<String> categories,
            @WebParam(name = "secondsBeforeArchiving") Integer secondsBeforeArchiving
    ) {
        return processDefinitionLogic.deployProcessDefinition(user, par, categories, secondsBeforeArchiving);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition redeployProcessDefinition(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "par") byte[] par,
            @WebParam(name = "categories") List<String> categories,
            @WebParam(name = "secondsBeforeArchiving") Integer secondsBeforeArchiving
    ) {
        return processDefinitionLogic.redeployProcessDefinition(user, processDefinitionVersionId, par, categories, secondsBeforeArchiving);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition updateProcessDefinition(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "processArchive") @NonNull byte[] processArchive
    ) {
        return processDefinitionLogic.updateProcessDefinition(user, processDefinitionVersionId, processArchive);
    }

    @Override
    public void setProcessDefinitionSubprocessBindingDate(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "date") Date date
    ) throws DefinitionDoesNotExistException {
        processDefinitionLogic.setProcessDefinitionSubprocessBindingDate(user, processDefinitionVersionId, date);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getLatestProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionName") @NonNull String definitionName) {
        return processDefinitionLogic.getLatestProcessDefinition(user, definitionName);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinitionVersion(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionName") @NonNull String definitionName,
            @WebParam(name = "definitionVersion") @NonNull Long definitionVersion
    ) {
        return processDefinitionLogic.getProcessDefinitionVersion(user, definitionName, definitionVersion);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinition(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId
    ) {
        return processDefinitionLogic.getProcessDefinition(user, processDefinitionVersionId);
    }

    @Override
    @WebResult(name = "result")
    public ParsedProcessDefinition getParsedProcessDefinition(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId
    ) {
        return processDefinitionLogic.getParsedProcessDefinition(user, processDefinitionVersionId);
    }

    @Override
    @WebResult(name = "result")
    public WfNode getNode(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "nodeId") @NonNull String nodeId
    ) {
        ParsedProcessDefinition pd = processDefinitionLogic.getDefinition(processDefinitionVersionId);
        Node node = pd.getNode(nodeId);
        if (node != null) {
            return new WfNode(node);
        }
        return null;
    }

    @Override
    @WebMethod(exclude = true)
    public List<WfDefinition> getDeployments(@NonNull User user, @NonNull BatchPresentation batchPresentation, boolean enablePaging) {
        return processDefinitionLogic.getDeployments(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitions(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "enablePaging") boolean enablePaging) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return processDefinitionLogic.getProcessDefinitions(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public int getProcessDefinitionsCount(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return processDefinitionLogic.getProcessDefinitionsCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public void undeployProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionName") @NonNull String definitionName, @WebParam(name = "version") Long version) {
        processDefinitionLogic.undeployProcessDefinition(user, definitionName, version);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getStartInteraction(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId
    ) {
        return processDefinitionLogic.getStartInteraction(user, processDefinitionVersionId);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getTaskNodeInteraction(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @NonNull String nodeId
    ) {
        ParsedProcessDefinition pd = processDefinitionLogic.getDefinition(processDefinitionVersionId);
        return pd.getInteractionNotNull(nodeId);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionFile(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "fileName") @NonNull String fileName
    ) {
        return processDefinitionLogic.getFile(user, processDefinitionVersionId, fileName);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionGraph(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "subprocessId") String subprocessId
    ) {
        return processDefinitionLogic.getGraph(user, processDefinitionVersionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<SwimlaneDefinition> getSwimlaneDefinitions(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId) {
        return processDefinitionLogic.getSwimlanes(user, processDefinitionVersionId);
    }

    @Override
    @WebResult(name = "result")
    public List<UserType> getUserTypes(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId
    ) {
        return processDefinitionLogic.getDefinition(processDefinitionVersionId).getUserTypes();
    }

    @Override
    @WebResult(name = "result")
    public UserType getUserType(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "name") @NonNull String name
    ) {
        return processDefinitionLogic.getDefinition(processDefinitionVersionId).getUserType(name);
    }

    @Override
    @WebMethod(exclude = true)
    public List<VariableDefinition> getVariableDefinitions(@NonNull User user, @NonNull Long processDefinitionVersionId) {
        return processDefinitionLogic.getProcessDefinitionVariables(user, processDefinitionVersionId);
    }

    @Override
    @WebResult(name = "result")
    public List<Variable> getVariableDefinitionsWS(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long processDefinitionVersionId) {
        List<VariableDefinition> variableDefinitions = getVariableDefinitions(user, processDefinitionVersionId);
        return VariableConverter.marshalDefinitions(variableDefinitions);
    }

    @Override
    @WebMethod(exclude = true)
    public VariableDefinition getVariableDefinition(@NonNull User user, @NonNull Long processDefinitionVersionId, @NonNull String variableName) {
        return processDefinitionLogic.getProcessDefinitionVariable(user, processDefinitionVersionId, variableName);
    }

    @Override
    @WebResult(name = "result")
    public Variable getVariableDefinitionWS(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long processDefinitionVersionId,
            @WebParam(name = "variableName") String variableName) {
        VariableDefinition variableDefinition = getVariableDefinition(user, processDefinitionVersionId, variableName);
        if (variableDefinition != null) {
            return VariableConverter.marshal(variableDefinition, null);
        }
        return null;
    }

    @Override
    @WebResult(name = "result")
    public List<NodeGraphElement> getProcessDefinitionGraphElements(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId, @WebParam(name = "subprocessId") String subprocessId) {
        return processDefinitionLogic.getProcessDefinitionGraphElements(user, processDefinitionVersionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitionHistory(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "name") @NonNull String name) {
        return processDefinitionLogic.getProcessDefinitionHistory(user, name);
    }

    public List<ProcessDefinitionChange> getChanges(@WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId) {
        throw new InternalApplicationException("method is defined here for compatibility only");
    }

    public List<ProcessDefinitionChange> getLastChanges(@WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId, @NonNull Long n) {
        throw new InternalApplicationException("method is defined here for compatibility only");
    }

    @Override
    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        return processDefinitionLogic.findChanges(definitionName, version1, version2);
    }

    @Override
    @WebResult(name = "result")
    public FileVariableImpl getFileVariableDefaultValue(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long definitionId,
            @WebParam(name = "variableName") @NonNull String variableName
    ) {
        WfVariable variable = variableLogic.getVariableDefaultValue(user, definitionId, variableName);
        if (variable != null) {
            FileVariable fileVariable = (FileVariable) variable.getValue();
            return new FileVariableImpl(fileVariable);
        }
        return null;
    }

}
