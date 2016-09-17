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
package ru.runa.wfe.service.delegate;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
import ru.runa.wfe.execution.dto.ProcessError;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;

/**
 * Created on 28.09.2004
 */
public class ExecutionServiceDelegate extends EJB3Delegate implements ExecutionService {

    public ExecutionServiceDelegate() {
        super(ExecutionService.class);
    }

    private ExecutionService getExecutionService() {
        return getService();
    }

    @Override
    public Long startProcess(User user, String definitionName, Map<String, Object> variablesMap) {
        try {
            return getExecutionService().startProcess(user, definitionName, variablesMap);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void cancelProcess(User user, Long processId) {
        try {
            getExecutionService().cancelProcess(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getProcessesCount(User user, BatchPresentation batchPresentation) {
        try {
            return getExecutionService().getProcessesCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfProcess> getProcesses(User user, BatchPresentation batchPresentation) {
        try {
            return getExecutionService().getProcesses(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfProcess> getProcessesByFilter(User user, ProcessFilter filter) {
        try {
            return getExecutionService().getProcessesByFilter(user, filter);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfProcess getProcess(User user, Long id) {
        try {
            return getExecutionService().getProcess(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfProcess getParentProcess(User user, Long id) {
        try {
            return getExecutionService().getParentProcess(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfProcess> getSubprocesses(User user, Long id, boolean recursive) {
        try {
            return getExecutionService().getSubprocesses(user, id, recursive);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfVariable> getVariables(User user, Long processId) {
        try {
            return getExecutionService().getVariables(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfVariable getVariable(User user, Long processId, String variableName) {
        try {
            return getExecutionService().getVariable(user, processId, variableName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfVariable getTaskVariable(User user, Long processId, Long taskId, String variableName) {
        try {
            return getExecutionService().getTaskVariable(user, processId, taskId, variableName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public FileVariable getFileVariableValue(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        try {
            return getExecutionService().getFileVariableValue(user, processId, variableName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        try {
            getExecutionService().updateVariables(user, processId, variables);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] getProcessDiagram(User user, Long processId, Long taskId, Long childProcessId, String subprocessId) {
        try {
            return getExecutionService().getProcessDiagram(user, processId, taskId, childProcessId, subprocessId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<NodeGraphElement> getProcessDiagramElements(User user, Long processId, String subprocessId) {
        try {
            return getExecutionService().getProcessDiagramElements(user, processId, subprocessId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public NodeGraphElement getProcessDiagramElement(User user, Long processId, String nodeId) {
        try {
            return getExecutionService().getProcessDiagramElement(user, processId, nodeId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeProcesses(User user, ProcessFilter filter) {
        try {
            getExecutionService().removeProcesses(user, filter);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<ProcessError> getProcessErrors(User user, Long processId) {
        try {
            return getExecutionService().getProcessErrors(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version) {
        try {
            return getExecutionService().upgradeProcessToDefinitionVersion(user, processId, version);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfSwimlane> getSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        try {
            return getExecutionService().getSwimlanes(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void assignSwimlane(User user, Long processId, String swimlaneName, Executor executor) throws ProcessDoesNotExistException {
        try {
            getExecutionService().assignSwimlane(user, processId, swimlaneName, executor);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfJob> getProcessJobs(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        try {
            return getExecutionService().getProcessJobs(user, processId, recursive);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfToken> getProcessTokens(User user, Long processId, boolean recursive) throws ProcessDoesNotExistException {
        try {
            return getExecutionService().getProcessTokens(user, processId, recursive);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void activateProcess(User user, Long processId) {
        try {
            getExecutionService().activateProcess(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void suspendProcess(User user, Long processId) {
        try {
            getExecutionService().suspendProcess(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
