package ru.runa.wf.web.action;

import java.util.List;

import ru.runa.common.web.action.AbstractExportExcelAction;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

/**
 * @since 4.3.0
 */
public class ExportExcelTasksAction extends AbstractExportExcelAction<WfTask> {

    @Override
    protected List<WfTask> getData(User user, BatchPresentation batchPresentation) {
        return Delegates.getTaskService().getMyTasks(user, batchPresentation);
    }

    @Override
    protected String getFileNamePrefix() {
        return "tasks";
    }
}
