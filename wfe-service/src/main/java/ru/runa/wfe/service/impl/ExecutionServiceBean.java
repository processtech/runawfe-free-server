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

import java.util.List;
import java.util.Map;
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
import ru.runa.wfe.ConfigurationException;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.ExecutionServiceLocal;
import ru.runa.wfe.service.decl.ExecutionServiceRemote;
import ru.runa.wfe.service.decl.ExecutionWebServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.service.jaxb.Variable;
import ru.runa.wfe.service.jaxb.VariableConverter;
import ru.runa.wfe.service.utils.FileVariablesUtil;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.dto.WfVariableHistoryState;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;
import ru.runa.wfe.var.logic.VariableLogic;

@Stateless(name = "ExecutionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "ExecutionAPI", serviceName = "ExecutionWebService")
@SOAPBinding
public class ExecutionServiceBean implements ExecutionServiceLocal, ExecutionServiceRemote, ExecutionWebServiceRemote {
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private VariableLogic variableLogic;

    @WebMethod(exclude = true)
    @Override
    public Long startProcess(@NonNull User user, @NonNull String definitionName, Map<String, Object> variables) {
        FileVariablesUtil.unproxyFileVariables(user, null, null, variables);
        return executionLogic.startProcess(user, definitionName, variables);
    }

    @WebMethod(exclude = true)
    @Override
    public Long startProcessById(@NonNull User user, @NonNull Long processDefinitionVersionId, Map<String, Object> variables) {
        FileVariablesUtil.unproxyFileVariables(user, null, null, variables);
        return executionLogic.startProcess(user, processDefinitionVersionId, variables);
    }

    @Override
    @WebResult(name = "result")
    public Long startProcessWS(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName,
            @WebParam(name = "variables") List<Variable> variables) {
        WfDefinition definition = processDefinitionLogic.getLatestProcessDefinition(user, definitionName);
        ParsedProcessDefinition parsedProcessDefinition = executionLogic.getDefinition(definition.getId());
        return startProcess(user, definitionName, VariableConverter.unmarshal(parsedProcessDefinition, variables));
    }

    @Override
    @WebResult(name = "result")
    public int getProcessesCount(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation
    ) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
        }
        return executionLogic.getProcessesCount(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public List<WfProcess> getProcesses(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
        }
        return executionLogic.getProcesses(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public WfProcess getProcess(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        return executionLogic.getProcess(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public WfProcess getParentProcess(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        return executionLogic.getParentProcess(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfProcess> getSubprocesses(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "recursive") boolean recursive) {
        return executionLogic.getSubprocesses(user, processId, recursive);
    }

    @WebMethod(exclude = true)
    @Override
    public List<WfVariable> getVariables(@NonNull User user, @NonNull Long processId) {
        List<WfVariable> list = variableLogic.getVariables(user, processId);
        for (WfVariable variable : list) {
            FileVariablesUtil.proxyFileVariables(user, processId, variable);
        }
        return list;
    }

    @WebMethod(exclude = true)
    @Override
    public Map<Long, List<WfVariable>> getVariables(@NonNull User user, @NonNull List<Long> processIds) {
        Map<Long, List<WfVariable>> result = variableLogic.getVariables(user, processIds);
        for (Map.Entry<Long, List<WfVariable>> entry : result.entrySet()) {
            for (WfVariable variable : entry.getValue()) {
                FileVariablesUtil.proxyFileVariables(user, entry.getKey(), variable);
            }
        }
        return result;
    }

    @WebMethod(exclude = true)
    @Override
    public WfVariableHistoryState getHistoricalVariables(@NonNull User user, @NonNull ProcessLogFilter filter) {
        long processId = filter.getProcessId();
        WfVariableHistoryState result = variableLogic.getHistoricalVariables(user, filter);
        for (WfVariable variable : result.getVariables()) {
            FileVariablesUtil.proxyFileVariables(user, processId, variable);
        }
        return result;
    }

    @WebMethod(exclude = true)
    @Override
    public WfVariableHistoryState getHistoricalVariables(@NonNull User user, @NonNull Long processId, Long taskId) {
        WfVariableHistoryState result = variableLogic.getHistoricalVariables(user, processId, taskId);
        for (WfVariable variable : result.getVariables()) {
            FileVariablesUtil.proxyFileVariables(user, processId, variable);
        }
        return result;
    }

    @Override
    @WebResult(name = "result")
    public List<Variable> getVariablesWS(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId) {
        List<WfVariable> variables = getVariables(user, processId);
        return VariableConverter.marshal(variables);
    }

    @WebMethod(exclude = true)
    @Override
    public WfVariable getVariable(@NonNull User user, @NonNull Long processId, @NonNull String variableName) {
        WfVariable variable = variableLogic.getVariable(user, processId, variableName);
        FileVariablesUtil.proxyFileVariables(user, processId, variable);
        return variable;
    }

    @Override
    @WebResult(name = "result")
    public Variable getVariableWS(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "variableName") String variableName) {
        WfVariable variable = getVariable(user, processId, variableName);
        if (variable != null) {
            return VariableConverter.marshal(variable.getDefinition(), variable.getValue());
        }
        return null;
    }

    @WebMethod(exclude = true)
    @Override
    public WfVariable getTaskVariable(@NonNull User user, @NonNull Long processId, @NonNull Long taskId, @NonNull String variableName) {
        WfVariable variable = variableLogic.getTaskVariable(user, processId, taskId, variableName);
        FileVariablesUtil.proxyFileVariables(user, processId, variable);
        return variable;
    }

    @Override
    @WebResult(name = "result")
    public FileVariable getFileVariableValue(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "variableName") @NonNull String variableName) {
        WfVariable variable = variableLogic.getVariable(user, processId, variableName);
        if (variable != null) {
            FileVariable fileVariable = (FileVariable) variable.getValue();
            return new FileVariableImpl(fileVariable);
        }
        return null;
    }

    @WebMethod(exclude = true)
    @Override
    public void updateVariables(@NonNull User user, @NonNull Long processId, @NonNull Map<String, Object> variables) {
        if (!SystemProperties.isUpdateProcessVariablesInAPIEnabled()) {
            throw new ConfigurationException(
                    "In order to enable script execution set property 'executionServiceAPI.updateVariables.enabled' to 'true' in system.properties or wfe.custom.system.properties");
        }
        FileVariablesUtil.unproxyFileVariables(user, processId, null, variables);
        variableLogic.updateVariables(user, processId, variables);
    }

    @Override
    @WebResult(name = "result")
    public void cancelProcess(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        executionLogic.cancelProcess(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfSwimlane> getSwimlanes(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        return executionLogic.getSwimlanes(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public void assignSwimlane(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "swimlaneName") @NonNull String swimlaneName, @WebParam(name = "executor") Executor executor) {
        executionLogic.assignSwimlane(user, processId, swimlaneName, executor);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDiagram(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "taskId") Long taskId, @WebParam(name = "childProcessId") Long childProcessId,
            @WebParam(name = "subprocessId") String subprocessId) {
        return executionLogic.getProcessDiagram(user, processId, taskId, childProcessId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<NodeGraphElement> getProcessDiagramElements(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "processId") @NonNull Long processId, @WebParam(name = "subprocessId") String subprocessId) {
        return executionLogic.getProcessDiagramElements(user, processId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public NodeGraphElement getProcessDiagramElement(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "processId") @NonNull Long processId, @WebParam(name = "nodeId") @NonNull String nodeId) {
        return executionLogic.getProcessDiagramElement(user, processId, nodeId);
    }

    @Override
    @WebResult(name = "result")
    public void removeProcesses(@WebParam(name = "user") @NonNull User user, @WebParam @NonNull ProcessFilter filter) {
        executionLogic.deleteProcesses(user, filter);
    }

    @Override
    @WebResult(name = "result")
    public boolean upgradeProcessToDefinitionVersion(@WebParam(name = "user") @NonNull User user,
            @WebParam(name = "processId") @NonNull Long processId, @WebParam(name = "version") Long version) {
        return executionLogic.upgradeProcessToDefinitionVersion(user, processId, version);
    }

    @Override
    @WebResult(name = "result")
    public int upgradeProcessesToDefinitionVersion(
            @WebParam(name = "user") @NonNull User user,
            @WebParam(name = "definitionId") @NonNull Long processDefinitionVersionId,
            @WebParam(name = "version") @NonNull Long newVersion
    ) {
        return executionLogic.upgradeProcessesToDefinitionVersion(user, processDefinitionVersionId, newVersion);
    }

    @Override
    @WebResult(name = "result")
    public void updateVariablesWS(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "variables") List<Variable> variables) {
        WfProcess process = executionLogic.getProcess(user, processId);
        ParsedProcessDefinition parsedProcessDefinition = executionLogic.getDefinition(process.getDefinitionId());
        updateVariables(user, processId, VariableConverter.unmarshal(parsedProcessDefinition, variables));
    }

    @Override
    @WebResult(name = "result")
    public List<WfJob> getProcessJobs(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "recursive") boolean recursive) {
        return executionLogic.getJobs(user, processId, recursive);
    }

    @Override
    @WebResult(name = "result")
    public List<WfToken> getProcessTokens(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId,
            @WebParam(name = "recursive") boolean recursive) {
        return executionLogic.getTokens(user, processId, recursive, false);
    }

    @Override
    @WebResult(name = "result")
    public void activateProcess(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        executionLogic.activateProcess(user, processId);
    }

    @Override
    @WebResult(name = "result")
    public void suspendProcess(@WebParam(name = "user") @NonNull User user, @WebParam(name = "processId") @NonNull Long processId) {
        executionLogic.suspendProcess(user, processId);
    }
}
