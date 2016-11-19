package ru.runa.wfe.extension.handler.var;

import java.util.Collections;
import java.util.List;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

public class SortListActionHandler extends CommonParamBasedHandler {

    @SuppressWarnings("rawtypes")
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<Comparable> list = handlerData.getInputParamValue(List.class, "list");
        String mode = handlerData.getInputParamValueNotNull(String.class, "mode");
        Collections.sort(list);
        if ("desc".equals(mode)) {
            Collections.reverse(list);
        }
        if (handlerData.getOutputParams().containsKey("result")) {
            handlerData.setOutputParam("result", list);
        } else {
            // back compatibility
            handlerData.setOutputParam("list", list);
        }
        log.debug("Sorted [" + mode + "] list " + list);
    }

}
