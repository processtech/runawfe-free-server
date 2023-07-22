package ru.runa.wfe.extension.handler.var;

import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

import com.google.common.collect.Maps;

public class GetObjectFromMapActionHandler extends CommonParamBasedHandler {
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map = handlerData.getInputParamValue(Map.class, "map");
        if (map == null) {
            map = Maps.newHashMap();
        }
        Object object = map.get(handlerData.getInputParamValueNotNull("key"));
        handlerData.setOutputParam("object", object);
    }

}
