package ru.runa.wfe.extension.handler.user;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.dao.ExecutorDAO;

public class GetTaskPerformerHandler extends CommonParamBasedHandler {

    @Autowired
    private ProcessLogDAO processLogDAO;

    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String nodeId = handlerData.getInputParamValueNotNull(String.class, "nodeId");
        ProcessLogFilter filter = new ProcessLogFilter(handlerData.getProcessId());
        filter.setNodeId(nodeId);
        ProcessLogs processLogs = new ProcessLogs();
        processLogs.addLogs(processLogDAO.getAll(filter), false);
        TaskEndLog taskEndLog = processLogs.getLastOrNull(TaskEndLog.class);
        if (taskEndLog == null) {
            throw new InternalApplicationException("No task end log found for node " + nodeId);
        }
        Object result = executorDAO.getExecutor(taskEndLog.getActorName());
        handlerData.setOutputParam("result", result);
    }

}
