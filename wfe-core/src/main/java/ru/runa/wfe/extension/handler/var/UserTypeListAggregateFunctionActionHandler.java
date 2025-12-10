package ru.runa.wfe.extension.handler.var;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.collect.Lists;

public class UserTypeListAggregateFunctionActionHandler extends CommonHandler {
    private UserTypeListAggregateFunctionConfig config;

    @Override
    public void setConfiguration(String configuration) {
        config = UserTypeListAggregateFunctionConfig.fromXml(configuration);
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        List<?> list = variableProvider.getValue(List.class, config.getListName());
        if (list == null) {
            list = Lists.newArrayList();
        }
        Map<String, Object> result = new HashMap<>();
        for (UserTypeListAggregateFunctionConfig.Operation operation : config.getOperations()) {
            List<Object> values = Lists.newArrayList();
            for (Object item : list) {
                if (item instanceof UserTypeMap) {
                    UserTypeMap userTypeMap = (UserTypeMap) item;
                    Object value = userTypeMap.get(operation.getAttribute());
                    if (value != null) {
                        values.add(value);
                    }
                }
            }
            Object aggregateResult = applyFunction(values, operation.getFunction());
            result.put(operation.getResult(), aggregateResult);
        }
        return result;
    }

    private Object applyFunction(List<Object> values, String function) throws Exception {
        if ("SUM".equals(function)) {
            return getSum(values);
        } else if ("AVERAGE".equals(function)) {
            long count = getCount(values);
            if (count == 0) {
                return 0;
            }
            double sum = getSum(values).doubleValue();
            return sum / count;
        } else if ("COUNT".equals(function)) {
            return getCount(values);
        } else if ("MIN".equals(function)) {
            return getMin(values);
        } else if ("MAX".equals(function)) {
            return getMax(values);
        } else {
            throw new Exception("Unknown function '" + function + "'");
        }
    }

    private Number getSum(List<Object> values) {
        boolean doubleValue = false;
        double sum = 0;
        for (Object object : values) {
            if (object instanceof Double) {
                doubleValue = true;
            }
            sum += TypeConversionUtil.convertTo(double.class, object);
        }
        if (doubleValue) {
            return sum;
        } else {
            return Long.valueOf((long) sum);
        }
    }

    private long getCount(List<Object> values) {
        return values.size();
    }

    private Number getMin(List<Object> values) {
        if (values.isEmpty()) {
            return 0;
        }
        boolean doubleValue = false;
        double min = Double.MAX_VALUE;
        for (Object object : values) {
            if (object instanceof Double) {
                doubleValue = true;
            }
            double d = TypeConversionUtil.convertTo(double.class, object);
            if (min > d) {
                min = d;
            }
        }
        if (doubleValue) {
            return min;
        } else {
            return Long.valueOf((long) min);
        }
    }

    private Number getMax(List<Object> values) {
        if (values.isEmpty()) {
            return 0;
        }
        boolean doubleValue = false;
        double max = Double.MIN_VALUE;
        for (Object object : values) {
            if (object instanceof Double) {
                doubleValue = true;
            }
            double d = TypeConversionUtil.convertTo(double.class, object);
            if (max < d) {
                max = d;
            }
        }
        if (doubleValue) {
            return max;
        } else {
            return Long.valueOf((long) max);
        }
    }

}