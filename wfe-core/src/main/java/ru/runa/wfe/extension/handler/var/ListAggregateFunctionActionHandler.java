package ru.runa.wfe.extension.handler.var;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;

import com.google.common.collect.Lists;

public class ListAggregateFunctionActionHandler extends CommonParamBasedHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<?> list = handlerData.getInputParamValue(List.class, "list");
        if (list == null) {
            list = Lists.newArrayList();
        }
        String function = handlerData.getInputParamValueNotNull(String.class, "function");
        Object result;
        if ("SUM".equals(function)) {
            result = getSum(list);
        } else if ("AVERAGE".equals(function)) {
            double sum = getSum(list).doubleValue();
            result = sum / getCount(list);
        } else if ("COUNT".equals(function)) {
            result = getCount(list);
        } else if ("MIN".equals(function)) {
            boolean doubleValue = false;
            double min = Double.MAX_VALUE;
            for (Object object : list) {
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
            for (Object object : list) {
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

    private Number getSum(List<?> list) {
        boolean doubleValue = false;
        double sum = 0;
        for (Object object : list) {
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

    private long getCount(List<?> list) {
        long count = 0;
        for (Object object : list) {
            if (object == null) {
                continue;
            }
            count++;
        }
        return count;
    }

}
