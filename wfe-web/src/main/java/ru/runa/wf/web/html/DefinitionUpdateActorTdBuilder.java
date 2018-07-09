package ru.runa.wf.web.html;

import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.user.Executor;

public class DefinitionUpdateActorTdBuilder extends BaseExecutorTdBuilder<WfDefinition> {

    @Override
    protected Executor getExecutor(WfDefinition object, Env env) {
        return object.getUpdateActor();
    }
}
