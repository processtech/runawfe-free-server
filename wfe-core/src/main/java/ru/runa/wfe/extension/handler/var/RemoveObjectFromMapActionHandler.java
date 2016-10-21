package ru.runa.wfe.extension.handler.var;

import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class RemoveObjectFromMapActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map map = handlerData.getInputParamValueNotNull(Map.class, "map");
        Object object = handlerData.getInputParamValueNotNull(Object.class, "key");
        if (object instanceof Map) {
            for (Object key : ((Map) object).keySet()) {
                map.remove(key);
            }
        } else {
            map.remove(object);
        }
        if (handlerData.getOutputParams().containsKey("result")) {
            handlerData.setOutputParam("result", map);
        } else {
            // back compatibility
            handlerData.setOutputParam("map", map);
        }
        log.debug("Object " + object + " removed from the map " + map);
    }

}
