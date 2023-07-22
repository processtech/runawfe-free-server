package ru.runa.wfe.extension.handler.var;

import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

import com.google.common.collect.Lists;

public class GetObjectFromListActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<?> list = handlerData.getInputParamValue(List.class, "list");
        if (list == null) {
            list = Lists.newArrayList();
        }
        int index = ListIndexUtils.adjustIndex(handlerData.getInputParamValueNotNull(int.class, "index"));
        Object object;
        if (list.size() > index) {
            object = list.get(index);
        } else {
            object = null;
        }
        handlerData.setOutputParam("object", object);
    }

}
