package ru.runa.wf.logic.bot;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.filter.DateFilterCriteria;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public class CancelOldProcesses extends TaskHandlerBase {

    @Override
    public void setConfiguration(String configuration) {
        // not used
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) {
        ExecutionService executionService = Delegates.getExecutionService();
        Date lastDate = new Date();
        long timeout = variableProvider.getValueNotNull(long.class, "timeout");
        lastDate.setTime(System.currentTimeMillis() - timeout * 3600 * 1000);
        BatchPresentation batchPresentation = BatchPresentationFactory.CURRENT_PROCESSES.createNonPaged();
        int endDateFieldIndex = ClassPresentationType.CURRENT_PROCESS.getFieldIndex(CurrentProcessClassPresentation.PROCESS_END_DATE);
        batchPresentation.getFilteredFields().put(endDateFieldIndex, new DateFilterCriteria());
        List<WfProcess> processes = executionService.getProcesses(user, batchPresentation);
        for (WfProcess process : processes) {
            if (process.getStartDate().before(lastDate) && !Objects.equal(process.getId(), task.getProcessId())) {
                executionService.cancelProcess(user, process.getId());
            }
        }
        Boolean periodic = variableProvider.getValueNotNull(Boolean.class, "isPeriodic");
        Map<String, Object> outVariables = Maps.newHashMap();
        outVariables.put(SKIP_TASK_COMPLETION_VARIABLE_NAME, periodic);
        return outVariables;
    }
}
