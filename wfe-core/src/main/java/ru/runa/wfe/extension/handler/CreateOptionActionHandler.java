package ru.runa.wfe.extension.handler;

import ru.runa.wfe.commons.web.Option;

public class CreateOptionActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String value = handlerData.getInputParamValueNotNull(String.class, "value");
        String text = handlerData.getInputParamValueNotNull(String.class, "text");
        handlerData.setOutputParam("option", new Option(value, text));
    }

}
