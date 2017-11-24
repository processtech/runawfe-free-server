package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class SetValueToListElementsAttributeActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        ArrayList list = handlerData.getInputParamValue(ArrayList.class, "list");
        String attribute = handlerData.getInputParamValueNotNull(String.class, "attribute");
        Object value = handlerData.getInputParamValueNotNull(Object.class, "value");
        if (list == null) {
            list = new ArrayList();
        }
        for (Object element : list) {
            if (element instanceof Map) {
                Map map = (Map) element;
                if (map.containsKey(attribute)) {
                    map.put(attribute, value);
                }
            }
        }
        if (handlerData.getOutputParams().containsKey("result")) {
            handlerData.setOutputParam("result", list);
        } else {
            // back compatibility
            handlerData.setOutputParam("list", list);
        }
        log.debug("Value " + value + " set to the list elements attribute " + attribute);
    }

}
