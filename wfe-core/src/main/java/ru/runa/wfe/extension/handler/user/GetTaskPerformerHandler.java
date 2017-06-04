package ru.runa.wfe.extension.handler.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.SwimlaneAssignLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
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
		List<ProcessLog> processLogs = processLogDAO.getAll(filter);
		Object result = null;
		for (ProcessLog processLog : processLogs) {
			if (processLog instanceof SwimlaneAssignLog) {
				Object[] patternArguments = ((SwimlaneAssignLog) processLog).getPatternArguments();
				String name = ((ExecutorNameValue) patternArguments[1]).getName();
				result = executorDAO.getExecutor(name);
				break;
			}
		}
		handlerData.setOutputParam("result", result);
	}

}
