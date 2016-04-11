package ru.runa.bp;

import java.util.Map;

import ru.runa.alfresco.AlfConnection;
import ru.runa.alfresco.RemoteAlfConnector;
import ru.runa.wfe.commons.TimeMeasurer;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Throwables;

/**
 * Base class for RunaWFE action handler and task handler.
 * 
 * @author dofs
 */
public abstract class AlfHandler extends TaskHandlerBase implements ActionHandler {
    private ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        paramsDef = ParamsDef.parse(configuration);
    }

    /**
     * Do work in Alfresco.
     * 
     * @param alfConnection
     *            alfresco connection
     * @param alfHandlerData
     *            parsed handler configuration
     * @throws Exception
     *             if error occurs
     */
    protected abstract void executeAction(AlfConnection alfConnection, AlfHandlerData alfHandlerData) throws Exception;

    /**
     * Do rollback in Alfresco on transaction rollback.
     * 
     * @param alfConnection
     *            alfresco connection
     * @param alfHandlerData
     *            parsed handler configuration
     * @throws Exception
     *             if error occurs TODO unsed yet in 4.x (move to wfe bots ?)
     */
    protected void onRollback(AlfConnection alfConnection, AlfHandlerData alfHandlerData) throws Exception {
        log.debug("onRollback in " + alfHandlerData.getProcessId());
    }

    @Override
    public void onRollback(final User user, final IVariableProvider variableProvider, final WfTask task) throws Exception {
        new RemoteAlfConnector<Object>() {
            @Override
            protected Object code() throws Exception {
                AlfHandlerData alfHandlerData = new AlfHandlerData(paramsDef, user, variableProvider, task);
                onRollback(alfConnection, alfHandlerData);
                return null;
            }
        }.runInSession();
    }

    @Override
    public void execute(ExecutionContext context) throws Exception {
        final AlfHandlerData handlerData = new AlfHandlerData(paramsDef, context);
        TimeMeasurer timeMeasurer = new TimeMeasurer(log);
        try {
            timeMeasurer.jobStarted();
            new RemoteAlfConnector<Object>() {
                @Override
                protected Object code() throws Exception {
                    executeAction(alfConnection, handlerData);
                    return null;
                }
            }.runInSession();
            context.setVariableValues(handlerData.getOutputVariables());
            timeMeasurer.jobEnded(handlerData.getTaskName());
        } catch (Throwable th) {
            throw Throwables.propagate(th);
        }
    }

    @Override
    public Map<String, Object> handle(final User user, final IVariableProvider variableProvider, final WfTask task) throws Exception {
        return new RemoteAlfConnector<Map<String, Object>>() {
            @Override
            protected Map<String, Object> code() throws Exception {
                AlfHandlerData handlerData = new AlfHandlerData(paramsDef, user, variableProvider, task);
                TimeMeasurer timeMeasurer = new TimeMeasurer(log);
                try {
                    timeMeasurer.jobStarted();
                    executeAction(alfConnection, handlerData);
                    timeMeasurer.jobEnded("Execution of " + handlerData.getTaskName());
                } catch (Throwable th) {
                    throw Throwables.propagate(th);
                }
                return handlerData.getOutputVariables();
            }
        }.runInSession();
    }

}
