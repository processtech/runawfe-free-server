package ru.runa.wfe.extension.handler.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

import com.google.common.collect.Maps;

public class ConvertMapValuesToListActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map = handlerData.getInputParamValue(Map.class, "map");
        if (map == null) {
            map = Maps.newHashMap();
        }
        List list = new ArrayList(map.values());
        handlerData.setOutputParam("list", list);
    }
}
