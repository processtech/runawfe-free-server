package ru.runa.wfe.extension.handler.var;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class MergeMapsActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map1 = handlerData.getInputParamValue(Map.class, "map1");
        if (map1 == null) {
            map1 = new HashMap();
        }
        Map<?, ?> map2 = handlerData.getInputParamValue(Map.class, "map2");
        if (map2 == null) {
            map2 = new HashMap();
        }
        Map resultMap = new HashMap(map1);
        resultMap.putAll(map2);
        handlerData.setOutputParam("map", resultMap);
        log.debug("Merged to the map " + resultMap);
    }

}
