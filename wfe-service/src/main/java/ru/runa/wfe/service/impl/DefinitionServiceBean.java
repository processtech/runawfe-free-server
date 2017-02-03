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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
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

import com.google.common.base.Preconditions;

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
    public WfDefinition deployProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "par") byte[] par,
            @WebParam(name = "categories") List<String> categories) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(par != null, "par");
        Preconditions.checkArgument(categories != null, "categories");
        return definitionLogic.deployProcessDefinition(user, par, categories);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition redeployProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "par") byte[] par, @WebParam(name = "categories") List<String> categories) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.redeployProcessDefinition(user, definitionId, par, categories);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition updateProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "processArchive") byte[] processArchive) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        Preconditions.checkArgument(processArchive != null, "processArchive");
        return definitionLogic.updateProcessDefinition(user, definitionId, processArchive);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getLatestProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionName != null, "definitionName");
        return definitionLogic.getLatestProcessDefinition(user, definitionName);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinitionVersion(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName,
            @WebParam(name = "definitionVersion") Long definitionVersion) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionName != null, "definitionName");
        Preconditions.checkArgument(definitionVersion != null, "definitionVersion");
        return definitionLogic.getProcessDefinitionVersion(user, definitionName, definitionVersion);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getProcessDefinition(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public ProcessDefinition getParsedProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getParsedProcessDefinition(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public Node getNode(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "nodeId") String nodeId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        Preconditions.checkArgument(nodeId != null, "nodeId");
        ProcessDefinition processDefinition = definitionLogic.getParsedProcessDefinition(user, definitionId);
        return processDefinition.getNode(nodeId);
    }

    @Override
    @WebMethod(exclude = true)
    public List<WfDefinition> getDeployments(User user, BatchPresentation batchPresentation, boolean enablePaging) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(batchPresentation != null, "batchPresentation");
        return definitionLogic.getDeployments(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitions(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation, @WebParam(name = "enablePaging") boolean enablePaging) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return definitionLogic.getProcessDefinitions(user, batchPresentation, enablePaging);
    }

    @Override
    @WebResult(name = "result")
    public int getProcessDefinitionsCount(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null, "user");
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return definitionLogic.getProcessDefinitionsCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public void undeployProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName,
            @WebParam(name = "version") Long version) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionName != null, "definitionName");
        definitionLogic.undeployProcessDefinition(user, definitionName, version);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getStartInteraction(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getStartInteraction(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getTaskNodeInteraction(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId, String nodeId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        Preconditions.checkArgument(nodeId != null, "nodeId");
        return definitionLogic.getTaskNodeInteraction(user, definitionId, nodeId);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionFile(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "fileName") String fileName) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        Preconditions.checkArgument(fileName != null, "fileName");
        return definitionLogic.getFile(user, definitionId, fileName);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionGraph(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "subprocessId") String subprocessId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getGraph(user, definitionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<SwimlaneDefinition> getSwimlaneDefinitions(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getSwimlanes(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public List<UserType> getUserTypes(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getDefinition(definitionId).getUserTypes();
    }

    @Override
    @WebResult(name = "result")
    public UserType getUserType(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "name") String name) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        Preconditions.checkArgument(name != null, "name");
        return definitionLogic.getDefinition(definitionId).getUserType(name);
    }

    @Override
    @WebMethod(exclude = true)
    public List<VariableDefinition> getVariableDefinitions(User user, Long definitionId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getProcessDefinitionVariables(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public List<Variable> getVariableDefinitionsWS(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        List<VariableDefinition> variableDefinitions = getVariableDefinitions(user, definitionId);
        return VariableConverter.marshalDefinitions(variableDefinitions);
    }

    @Override
    @WebMethod(exclude = true)
    public VariableDefinition getVariableDefinition(User user, Long definitionId, String variableName) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        Preconditions.checkArgument(variableName != null, "variableName");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getProcessDefinitionVariable(user, definitionId, variableName);
    }

    @Override
    @WebResult(name = "result")
    public Variable getVariableDefinitionWS(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "variableName") String variableName) {
        VariableDefinition variableDefinition = getVariableDefinition(user, definitionId, variableName);
        if (variableDefinition != null) {
            return VariableConverter.marshal(variableDefinition, null);
        }
        return null;
    }

    @Override
    @WebResult(name = "result")
    public List<NodeGraphElement> getProcessDefinitionGraphElements(@WebParam(name = "user") User user,
            @WebParam(name = "definitionId") Long definitionId, @WebParam(name = "subprocessId") String subprocessId) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(definitionId != null, "definitionId");
        return definitionLogic.getProcessDefinitionGraphElements(user, definitionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitionHistory(@WebParam(name = "user") User user, @WebParam(name = "name") String name) {
        Preconditions.checkArgument(user != null, "user");
        Preconditions.checkArgument(name != null, "name");
        return definitionLogic.getProcessDefinitionHistory(user, name);
    }

    public List<ProcessDefinitionChange> getChanges(Long definitionId){
        return definitionLogic.getChanges(definitionId);
    }

    public List<ProcessDefinitionChange> findChanges(String definitionName, Long version1, Long version2){
        return definitionLogic.findChanges(definitionName, version1, version2);
    }

    public List<ProcessDefinitionChange> findChangesWithin(Date date1, Date date2){
        return definitionLogic.findChanges(date1, date2);
    }
}
