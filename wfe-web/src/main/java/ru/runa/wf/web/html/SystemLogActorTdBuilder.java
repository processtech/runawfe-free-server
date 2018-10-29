package ru.runa.wf.web.html;

import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.user.Executor;

/**
 * Builds table cell for {@link Executor}, executed action.
 */
public class SystemLogActorTdBuilder extends BaseExecutorTdBuilder<SystemLog> {

    /**
     * Creates component to build table cell for {@link Executor}, executed action.
     */
    public SystemLogActorTdBuilder() {
        setSecuredObjectExtractor(new ExecutorExtractor());
    }

    @Override
    protected boolean isEnabled(Object object, Env env) {
        return true;
    }

    @Override
    protected Executor getExecutor(SystemLog object, Env env) {
        return (Executor) getExtractor().getSecuredObject(object.getActorId(), env);
    }
}
