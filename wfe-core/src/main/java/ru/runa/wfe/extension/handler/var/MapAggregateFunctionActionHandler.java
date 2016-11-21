package ru.runa.wfe.extension.handler.var;

import java.util.Collection;
import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

import com.google.common.collect.Maps;

public class MapAggregateFunctionActionHandler extends CommonParamBasedHandler {
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Map<?, ?> map = handlerData.getInputParamValue(Map.class, "map");
        if (map == null) {
            map = Maps.newHashMap();
        }
        String function = handlerData.getInputParamValueNotNull(String.class, "function");
        String functionOn = handlerData.getInputParamValueNotNull(String.class, "on");
        Collection<?> collection;
        if ("KEYS".equals(functionOn)) {
            collection = map.keySet();
        } else {
            collection = map.values();
        }
        Object result;
        if ("SUM".equals(function)) {
            result = getSum(collection);
        } else if ("AVERAGE".equals(function)) {
            double sum = getSum(collection).doubleValue();
            result = sum / collection.size();
        } else if ("COUNT".equals(function)) {
            result = collection.size();
        } else if ("MIN".equals(function)) {
            boolean doubleValue = false;
            double min = Double.MAX_VALUE;
            for (Object object : collection) {
                if (object instanceof Double) {
                    doubleValue = true;
                }
                double d = TypeConversionUtil.convertTo(double.class, object);
                if (min > d) {
                    min = d;
                }
            }
            if (doubleValue) {
                result = min;
            } else {
                result = new Long((long) min);
            }
        } else if ("MAX".equals(function)) {
            boolean doubleValue = false;
            double max = Double.MIN_VALUE;
            for (Object object : collection) {
                if (object instanceof Double) {
                    doubleValue = true;
                }
                double d = TypeConversionUtil.convertTo(double.class, object);
                if (max < d) {
                    max = d;
                }
            }
            if (doubleValue) {
                result = max;
            } else {
                result = new Long((long) max);
            }
        } else {
            throw new Exception("Unknown function '" + function + "'");
        }
        handlerData.setOutputParam("object", result);
    }

    private Number getSum(Collection<?> collection) {
        boolean doubleValue = false;
        double sum = 0;
        for (Object object : collection) {
            if (object instanceof Double) {
                doubleValue = true;
            }
            sum += TypeConversionUtil.convertTo(double.class, object);
        }
        if (doubleValue) {
            return sum;
        } else {
            return new Long((long) sum);
        }
    }

}
