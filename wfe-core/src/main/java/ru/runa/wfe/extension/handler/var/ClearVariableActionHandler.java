package ru.runa.wfe.extension.handler.var;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.extension.handler.ParamDef;

/**
 * Set process variable to 'null' value.
 * 
 * @author dofs
 * @since 3.4
 */
public class ClearVariableActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        ParamDef paramDef = handlerData.getOutputParamNotNull("object");
        handlerData.setOutputVariable(paramDef.getVariableName(), null);
    }

}
