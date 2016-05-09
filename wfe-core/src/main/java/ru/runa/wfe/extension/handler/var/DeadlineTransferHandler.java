package ru.runa.wfe.extension.handler.var;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.Timer;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.var.IVariableProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DeadlineTransferHandler extends CommonHandler {
    private DeadlineTransferConfig config;
    @Autowired
    private BusinessCalendar businessCalendar;

    @Override
    public void setConfiguration(String configuration) {
        config = DeadlineTransferConfig.parse(configuration);
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        long processId = (Long)variableProvider.getValue(config.getProcessId());
        Process process = ApplicationContextFactory.getProcessDAO().getNotNull(processId);
        ProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(process);
        ExecutionContext context = new ExecutionContext(processDefinition, process);
        String variableName;
        if (config.isInputVariable()) {
            variableName = config.getVariableName();
        } else {
            variableName = (String)variableProvider.getVariable(config.getVariableName()).getValue();
        }
        log.debug("Load data, p_id = " + processId + " ;Variable = " + variableName);


        Calendar calendar = Calendar.getInstance();
        for (DeadlineTransferConfig.CalendarOperation operation : config.getOperations()) {
            log.debug("Executing " + operation + " on " + CalendarUtil.formatDateTime(calendar));
            Integer amount = Integer.parseInt(operation.getExpression());
            if (DeadlineTransferConfig.CalendarOperation.ADD.equals(operation.getType())) {
                BusinessDuration duration = new BusinessDuration(operation.getField(), amount, operation.isBusinessTime());
                Date date = businessCalendar.apply(calendar.getTime(), duration);
                calendar.setTime(date);
            }
            if (DeadlineTransferConfig.CalendarOperation.SET.equals(operation.getType())) {
                calendar.set(operation.getField(), amount);
            }
        }
        context.setVariableValue(variableName, calendar.getTime());

        return null;
    }
}
