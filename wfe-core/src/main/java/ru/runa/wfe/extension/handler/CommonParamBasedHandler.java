package ru.runa.wfe.extension.handler;

import com.google.common.base.Throwables;
import java.util.Map;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Base class for standard XML parameter-based configuration.
 * 
 * @author dofs[197@gmail.com]
 */
public abstract class CommonParamBasedHandler extends TaskHandlerBase implements ActionHandler {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) {
        paramsDef = ParamsDef.parse(configuration);
    }

    protected abstract void executeAction(HandlerData handlerData) throws Exception;

    @Override
    public void execute(ExecutionContext context) throws Exception {
        final HandlerData handlerData = new HandlerData(paramsDef, context);
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        try {
            timeMeasurer.jobStarted();
            executeAction(handlerData);
            context.setVariableValues(handlerData.getOutputVariables());
            timeMeasurer.jobEnded(handlerData.getTaskName());
        } catch (Throwable th) {
            throw Throwables.propagate(th);
        }
    }

    @Override
    public Map<String, Object> handle(final User user, final VariableProvider variableProvider, final WfTask task) {
        HandlerData handlerData = new HandlerData(paramsDef, user, variableProvider, task);
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        try {
            timeMeasurer.jobStarted();
            executeAction(handlerData);
            timeMeasurer.jobEnded("Execution of " + handlerData.getTaskName());
        } catch (Throwable th) {
            throw Throwables.propagate(th);
        }
        return handlerData.getOutputVariables();
    }
}
