package ru.runa.wf.web.html;

import ru.runa.wfe.task.dto.WfTask;

public class TaskRootProcessDefinitionTdBuilder extends TaskProcessDefinitionTdBuilder {
    @Override
    protected Long getDefinitionId(WfTask task) {
        return task.getRootDefinitionId();
    }

    @Override
    public String getValue(Object object, Env env) {
        return ((WfTask) object).getRootDefinitionName();
    }
}
