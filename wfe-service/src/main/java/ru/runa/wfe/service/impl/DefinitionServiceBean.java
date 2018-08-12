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
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.DefinitionServiceLocal;
import ru.runa.wfe.service.decl.DefinitionServiceRemote;
import ru.runa.wfe.service.decl.DefinitionServiceRemoteWS;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.service.jaxb.VariableConverter;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;

@Stateless(name = "DefinitionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "DefinitionAPI", serviceName = "DefinitionWebService")
@SOAPBinding
public class DefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote, DefinitionServiceRemoteWS {
    @Autowired
    private DefinitionLogic definitionLogic;

    @Override
    @WebResult(name = "result")
    public WfDefinition deployProcessDefinition(@WebParam(name = "user") @NonNull User user, @WebParam(name = "par") @NonNull byte[] par,
            @WebParam(name = "categories") @NonNull List<String> categories) {
        return definitionLogic.deployProcessDefinition(user, par, categories);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition redeployProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId, @WebParam(name = "par") byte[] par,
            @WebParam(name = "categories") List<String> categories) {
        return definitionLogic.redeployProcessDefinition(user, deploymentVersionId, par, categories);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition updateProcessDefinition(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @WebParam(name = "processArchive") @NonNull byte[] processArchive
    ) {
        return definitionLogic.updateProcessDefinition(user, deploymentVersionId, processArchive);
    }

    @Override
    public void setProcessDefinitionSubprocessBindingDate(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @WebParam(name = "date") Date date
    ) throws DefinitionDoesNotExistException {
        definitionLogic.setProcessDefinitionSubprocessBindingDate(user, deploymentVersionId, date);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getLatestProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionName") @NonNull String definitionName) {
        return definitionLogic.getLatestProcessDefinition(user, definitionName);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinitionVersion(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionName") @NonNull String definitionName,
            @WebParam(name = "definitionVersion") @NonNull Long definitionVersion) {
        return definitionLogic.getProcessDefinitionVersion(user, definitionName, definitionVersion);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId) {
        return definitionLogic.getProcessDefinition(user, deploymentVersionId);
    }

    @Override
    @WebResult(name = "result")
    public ProcessDefinition getParsedProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId) {
        return definitionLogic.getParsedProcessDefinition(user, deploymentVersionId);
    }

    @Override
    @WebResult(name = "result")
    public WfNode getNode(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @WebParam(name = "nodeId") @NonNull String nodeId
    ) {
        ProcessDefinition pd = definitionLogic.getDefinition(deploymentVersionId);
        Node node = pd.getNode(nodeId);
        if (node != null) {
            return new WfNode(node);
        }
        return null;
    }

    @Override
    @WebMethod(exclude = true)
    public List<WfDefinition> getDeployments(@NonNull User user, @NonNull BatchPresentation batchPresentation, boolean enablePaging) {
        return definitionLogic.getDeployments(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitions(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "enablePaging") boolean enablePaging) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return definitionLogic.getProcessDefinitions(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public int getProcessDefinitionsCount(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return definitionLogic.getProcessDefinitionsCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public void undeployProcessDefinition(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionName") @NonNull String definitionName, @WebParam(name = "version") Long version) {
        definitionLogic.undeployProcessDefinition(user, definitionName, version);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getStartInteraction(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId
    ) {
        return definitionLogic.getStartInteraction(user, deploymentVersionId);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getTaskNodeInteraction(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @NonNull String nodeId
    ) {
        ProcessDefinition pd = definitionLogic.getDefinition(deploymentVersionId);
        return pd.getInteractionNotNull(nodeId);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionFile(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @WebParam(name = "fileName") @NonNull String fileName
    ) {
        return definitionLogic.getFile(user, deploymentVersionId, fileName);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionGraph(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @WebParam(name = "subprocessId") String subprocessId
    ) {
        return definitionLogic.getGraph(user, deploymentVersionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<SwimlaneDefinition> getSwimlaneDefinitions(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long definitionId) {
        return definitionLogic.getSwimlanes(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public List<UserType> getUserTypes(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId
    ) {
        return definitionLogic.getDefinition(deploymentVersionId).getUserTypes();
    }

    @Override
    @WebResult(name = "result")
    public UserType getUserType(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId,
            @WebParam(name = "name") @NonNull String name) {
        return definitionLogic.getDefinition(deploymentVersionId).getUserType(name);
    }

    @Override
    @WebMethod(exclude = true)
    public List<VariableDefinition> getVariableDefinitions(@NonNull User user, @NonNull Long deploymentVersionId) {
        return definitionLogic.getProcessDefinitionVariables(user, deploymentVersionId);
    }

    @Override
    @WebResult(name = "result")
    public List<Variable> getVariableDefinitionsWS(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long deploymentVersionId) {
        List<VariableDefinition> variableDefinitions = getVariableDefinitions(user, deploymentVersionId);
        return VariableConverter.marshalDefinitions(variableDefinitions);
    }

    @Override
    @WebMethod(exclude = true)
    public VariableDefinition getVariableDefinition(@NonNull User user, @NonNull Long deploymentVersionId, @NonNull String variableName) {
        return definitionLogic.getProcessDefinitionVariable(user, deploymentVersionId, variableName);
    }

    @Override
    @WebResult(name = "result")
    public Variable getVariableDefinitionWS(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long deploymentVersionId,
            @WebParam(name = "variableName") String variableName) {
        VariableDefinition variableDefinition = getVariableDefinition(user, deploymentVersionId, variableName);
        if (variableDefinition != null) {
            return VariableConverter.marshal(variableDefinition, null);
        }
        return null;
    }

    @Override
    @WebResult(name = "result")
    public List<NodeGraphElement> getProcessDefinitionGraphElements(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long deploymentVersionId, @WebParam(name = "subprocessId") String subprocessId) {
        return definitionLogic.getProcessDefinitionGraphElements(user, deploymentVersionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitionHistory(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "name") @NonNull String name) {
        return definitionLogic.getProcessDefinitionHistory(user, name);
    }

    @Override
    public List<ProcessDefinitionChange> getChanges(@WebParam(name = "definitionId") @NonNull Long deploymentVersionId) {
        return definitionLogic.getChanges(deploymentVersionId);
    }

    @Override
    public List<ProcessDefinitionChange> getLastChanges(@WebParam(name = "definitionId") @NonNull Long deploymentVersionId, @NonNull Long n) {
        return definitionLogic.getLastChanges(deploymentVersionId, n.intValue());
    }

    @Override
    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2) {
        return definitionLogic.findChanges(definitionName, version1, version2);
    }
}
