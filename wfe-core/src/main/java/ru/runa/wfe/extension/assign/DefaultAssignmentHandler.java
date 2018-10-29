package ru.runa.wfe.extension.assign;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.extension.AssignmentHandler;
import ru.runa.wfe.user.Executor;

public class DefaultAssignmentHandler implements AssignmentHandler {
    protected final Log log = LogFactory.getLog(getClass());
    protected String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    protected List<? extends Executor> calculateExecutors(ExecutionContext executionContext, Assignable assignable) {
        return SwimlaneInitializerHelper.evaluate(configuration, executionContext.getVariableProvider());
    }

    @Override
    public void assign(ExecutionContext executionContext, Assignable assignable) {
        List<? extends Executor> executors = calculateExecutors(executionContext, assignable);
        AssignmentHelper.assign(executionContext, assignable, executors);
    }
}
