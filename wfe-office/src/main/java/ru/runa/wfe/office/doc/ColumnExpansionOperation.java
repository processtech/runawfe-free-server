package ru.runa.wfe.office.doc;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;

public class ColumnExpansionOperation extends AbstractIteratorOperation {
    private String containerSelector;

    public void setContainerSelector(String containerSelector) {
        this.containerSelector = containerSelector;
    }

    @Override
    public void setContainerVariable(WfVariable containerVariable) {
        super.setContainerVariable(containerVariable);
        if (iterateBy == null) {
            if (containerVariable.getValue() instanceof Map) {
                iterateBy = IterateBy.values;
            }
            if (containerVariable.getValue() instanceof List) {
                iterateBy = IterateBy.items;
            }
        }
    }

    public String getStringValue(DocxConfig config, VariableProvider variableProvider, Object key) {
        if (iterateBy == IterateBy.indexes) {
            return String.valueOf(key);
        }
        if (iterateBy == IterateBy.numbers) {
            return String.valueOf(((Number) key).longValue() + 1);
        }
        if (iterateBy == IterateBy.items) {
            int index = TypeConversionUtil.convertTo(Integer.class, key);
            List<?> list = (List<?>) containerVariable.getValue();
            Object listItem = list.size() > index ? list.get(index) : null;
            if (containerSelector == null) {
                return FormatCommons.formatComponentValue(containerVariable, 0, listItem);
            } else {
                Object containerValue = DocxUtils.getValue(config, variableProvider, listItem, containerSelector);
                if (containerValue == null) {
                    return "";
                } else {
                    return String.valueOf(containerValue);
                }
            }
        }
        if (iterateBy == IterateBy.keys) {
            if (containerSelector == null) {
                return FormatCommons.formatComponentValue(containerVariable, 0, key);
            } else {
                return String.valueOf(DocxUtils.getValue(config, variableProvider, key, containerSelector));
            }
        }
        if (iterateBy == IterateBy.values) {
            Object value = ((Map<?, ?>) containerVariable.getValue()).get(key);
            if (containerSelector == null) {
                return FormatCommons.formatComponentValue(containerVariable, 1, value);
            } else {
                return String.valueOf(DocxUtils.getValue(config, variableProvider, value, containerSelector));
            }
        }
        return null;
    }

}
