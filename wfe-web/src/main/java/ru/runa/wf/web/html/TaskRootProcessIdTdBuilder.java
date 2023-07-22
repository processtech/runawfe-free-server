package ru.runa.wf.web.html;

import ru.runa.wfe.task.dto.WfTask;

public class TaskRootProcessIdTdBuilder extends TaskProcessIdTdBuilder {
    @Override
    protected Long getProcessId(WfTask task) {
        return task.getRootProcessId();
    }
}
