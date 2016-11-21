package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class SetObjectToListActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("unchecked")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        ArrayList list = handlerData.getInputParamValue(ArrayList.class, "list");
        Object object = handlerData.getInputParamValueNotNull(Object.class, "object");
        int index = ListIndexUtils.adjustIndex(handlerData.getInputParamValueNotNull(int.class, "index"));
        if (list == null) {
            list = new ArrayList();
        }
        for (int i = list.size(); i <= index; i++) {
            list.add(null);
        }
        list.set(index, object);
        if (handlerData.getOutputParams().containsKey("result")) {
            handlerData.setOutputParam("result", list);
        } else {
            // back compatibility
            handlerData.setOutputParam("list", list);
        }
        log.debug("Object " + object + " set to the list " + list + " at index " + index);
    }

}
