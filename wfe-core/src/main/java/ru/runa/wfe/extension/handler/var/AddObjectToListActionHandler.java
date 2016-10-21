package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class AddObjectToListActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List list = handlerData.getInputParamValue(List.class, "list");
        if (list == null) {
            list = new ArrayList();
        }
        Integer index = ListIndexUtils.adjustIndex(handlerData.getInputParamValue(Integer.class, "index"));
        Object object = handlerData.getInputParamValueNotNull(Object.class, "object");
        if (object instanceof Collection) {
            if (index != null) {
                list.addAll(index, (Collection) object);
            } else {
                list.addAll((Collection) object);
            }
        } else {
            if (index != null) {
                list.add(index, object);
            } else {
                list.add(object);
            }
        }
        if (handlerData.getOutputParams().containsKey("result")) {
            handlerData.setOutputParam("result", list);
        } else {
            // back compatibility
            handlerData.setOutputParam("list", list);
        }
        log.debug("Object " + object + " added to the list " + list);
    }

}
