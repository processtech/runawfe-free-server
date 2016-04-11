package ru.runa.bp;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Parameters holder for Alfresco handler.
 * 
 * @author dofs
 */
public class AlfHandlerData extends HandlerData {
    // this class does not removed due to wide usage only

    public AlfHandlerData(ParamsDef paramsDef, ExecutionContext context) {
        super(paramsDef, context);
    }

    public AlfHandlerData(ParamsDef paramsDef, User user, IVariableProvider variableProvider, WfTask task) {
        super(paramsDef, user, variableProvider, task);
    }

}
