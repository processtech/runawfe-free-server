package ru.runa.wfe.extension.handler.var;

import java.util.Collection;
import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class RemoveObjectFromListActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<?> list = handlerData.getInputParamValueNotNull(List.class, "list");
        Object object = handlerData.getInputParamValueNotNull(Object.class, "object");
        if (object instanceof Collection) {
            list.removeAll((Collection<?>) object);
        } else {
            list.remove(object);
        }
        if (handlerData.getOutputParams().containsKey("result")) {
            handlerData.setOutputParam("result", list);
        } else {
            // back compatibility
            handlerData.setOutputParam("list", list);
        }
        log.debug("Object " + object + " removed from the list " + list);
    }

}
