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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.chat.ChatMessage;
import ru.runa.wfe.chat.ChatsUserInfo;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessFilter;
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
import ru.runa.wfe.var.dto.WfVariableHistoryState;
import ru.runa.wfe.var.file.FileVariableImpl;

/**
 * Created on 28.09.2004
 */
public class ExecutionServiceDelegate extends Ejb3Delegate implements ExecutionService {
    //
    @Override
    public List<ChatMessage> getChatMessages(int chatId) {
        return getExecutionService().getChatMessages(chatId);
    }

    @Override
    public ChatMessage getChatMessage(int chatId, long messageId) {
        return getExecutionService().getChatMessage(chatId, messageId);
    }

    @Override
    public List<ChatMessage> getChatMessages(int chatId, int firstId, int count) {
        return getExecutionService().getChatMessages(chatId, firstId, count);
    }

    @Override
    public List<ChatMessage> getChatFirstMessages(int chatId, int count) {
        return getExecutionService().getChatFirstMessages(chatId, count);
    }

    @Override
    public void deleteChatMessage(long messId) {
        getExecutionService().deleteChatMessage(messId);
    }

    @Override
    public ChatsUserInfo getChatUserInfo(long userId, String userName, int chatId) {
        return getExecutionService().getChatUserInfo(userId, userName, chatId);
    }

    @Override
    public long getChatNewMessagesCount(long lastMessageId, int chatId) {
        return getExecutionService().getChatNewMessagesCount(lastMessageId, chatId);
    }

    @Override
    public void updateChatUserInfo(long userId, String userName, int chatId, long lastMessageId) {
        getExecutionService().updateChatUserInfo(userId, userName, chatId, lastMessageId);
    }

    @Override
    public long getChatAllMessagesCount(int chatId) {
        return getExecutionService().getChatAllMessagesCount(chatId);
    }

    @Override
    public List<Integer> getChatAllConnectedChatId(int chatId) {
        return getExecutionService().getChatAllConnectedChatId(chatId);
    }

    @Override
    public long setChatMessage(int chatId, ChatMessage message) {
        return getExecutionService().setChatMessage(chatId, message);
    }

    //
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
    public Long startProcessById(User user, Long definitionId, Map<String, Object> variablesMap) {
        try {
            return getExecutionService().startProcessById(user, definitionId, variablesMap);
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
            ExecutionService a = getExecutionService();
            return getExecutionService().getVariables(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Map<Long, List<WfVariable>> getVariables(User user, List<Long> processIds) {
        try {
            return getExecutionService().getVariables(user, processIds);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfVariableHistoryState getHistoricalVariables(User user, ProcessLogFilter filter) {
        try {
            return getExecutionService().getHistoricalVariables(user, filter);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        try {
            return getExecutionService().getHistoricalVariables(user, processId, taskId);
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
    public FileVariableImpl getFileVariableValue(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
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
    public boolean upgradeProcessToDefinitionVersion(User user, Long processId, Long version) {
        try {
            return getExecutionService().upgradeProcessToDefinitionVersion(user, processId, version);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int upgradeProcessesToDefinitionVersion(User user, Long definitionId, Long newVersion) {
        try {
            return getExecutionService().upgradeProcessesToDefinitionVersion(user, definitionId, newVersion);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfSwimlane> getProcessSwimlanes(User user, Long processId) throws ProcessDoesNotExistException {
        try {
            return getExecutionService().getProcessSwimlanes(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfSwimlane> getActiveProcessesSwimlanes(User user, String namePattern) {
        return getExecutionService().getActiveProcessesSwimlanes(user, namePattern);
    }

    @Override
    public boolean reassignSwimlane(User user, Long id) {
        return getExecutionService().reassignSwimlane(user, id);
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

    @Override
    public void sendSignal(User user, Map<String, String> routingData, Map<String, Object> payloadData, long ttl) {
        try {
            getExecutionService().sendSignal(user, routingData, payloadData, ttl);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean signalReceiverIsActive(User user, Map<String, String> routingData) {
        try {
            return getExecutionService().signalReceiverIsActive(user, routingData);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
