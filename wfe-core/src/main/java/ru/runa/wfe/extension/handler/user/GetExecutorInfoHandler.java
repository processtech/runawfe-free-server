package ru.runa.wfe.extension.handler.user;

import org.apache.commons.beanutils.PropertyUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class GetExecutorInfoHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        validateOutputVariableDataType(handlerData);
        Executor executor = handlerData.getInputParamValueNotNull(Executor.class, "executor");
        String format = handlerData.getInputParamValueNotNull("format");
        Object result = PropertyUtils.getProperty(executor, format);
        handlerData.setOutputParam("result", result);
    }

    private void validateOutputVariableDataType(HandlerData handlerData) {
        String outputVariableName = handlerData.getOutputParamNotNull("result").getVariableName();
        VariableFormat outputVariableFormat = handlerData.getExecutionContext().getParsedProcessDefinition().getVariable(outputVariableName, false)
                .getFormatNotNull();
        if (!(outputVariableFormat instanceof StringFormat)) {
            throw new InternalApplicationException("Not supported since v4.4.3");
        }
    }
}
