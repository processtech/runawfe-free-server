package ru.runa.wfe.rest.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.format.VariableFormatVisitor;

public abstract class BaseWfVariableFormatVisitor implements VariableFormatVisitor<Object, WfVariable> {

    @Override
    public Object onDate(DateFormat dateFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onTime(TimeFormat timeFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onDateTime(DateTimeFormat dateTimeFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onBoolean(BooleanFormat booleanFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onBigDecimal(BigDecimalFormat bigDecimalFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onDouble(DoubleFormat doubleFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onLong(LongFormat longFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onHidden(HiddenFormat hiddenFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onList(ListFormat listFormat, WfVariable variable) {
        if (variable.getValue() == null) {
            return null;
        }
        List<Object> list = (List<Object>) variable.getValue();
        String name = variable.getDefinition().getName() +
                VariableFormatContainer.COMPONENT_QUALIFIER_START + "i" + VariableFormatContainer.COMPONENT_QUALIFIER_END;
        return onList(list, new VariableDefinition(name, null, listFormat.getComponentClassName(0), listFormat.getComponentUserType(0)));
    }

    abstract List<Object> onList(List<Object> values, VariableDefinition definition);

    @Override
    public Object onMap(MapFormat mapFormat, WfVariable variable) {
        if (variable.getValue() == null) {
            return null;
        }
        Map<Object, Object> map = (Map<Object, Object>) variable.getValue();
        Map<Object, Object> result = new HashMap<>();
        String nameTemplate = variable.getDefinition().getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + "i%s"
                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            VariableDefinition keyDefinition = new VariableDefinition(String.format(nameTemplate, VariableFormatContainer.MAP_KEY_SUFFIX), null,
                    mapFormat.getComponentClassName(0), mapFormat.getComponentUserType(0));
            VariableDefinition valueDefinition = new VariableDefinition(String.format(nameTemplate, VariableFormatContainer.MAP_VALUE_SUFFIX), null,
                    mapFormat.getComponentClassName(1), mapFormat.getComponentUserType(1));
            result.put(keyDefinition.getFormatNotNull().processBy(this, new WfVariable(keyDefinition, entry.getKey())),
                    valueDefinition.getFormatNotNull().processBy(this, new WfVariable(valueDefinition, entry.getValue())));
        }
        return result;
    }

    @Override
    public Object onProcessId(ProcessIdFormat processIdFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onString(StringFormat stringFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onTextString(TextFormat textFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onFormattedTextString(FormattedTextFormat textFormat, WfVariable variable) {
        return variable.getValue();
    }

    @Override
    public Object onUserType(UserTypeFormat userTypeFormat, WfVariable variable) {
        if (variable.getValue() == null) {
            return null;
        }
        Map<String, Object> value = (Map<String, Object>) variable.getValue();
        Map<String, Object> result = getCustomUserTypeFields(userTypeFormat);
        String namePrefix = variable.getDefinition().getName() + UserType.DELIM;
        for (VariableDefinition attributeDefinition : userTypeFormat.getUserType().getAttributes()) {
            Object attributeValue = value.get(attributeDefinition.getName());
            VariableDefinition attributeVariable = new VariableDefinition(namePrefix + attributeDefinition.getName(), null, attributeDefinition);
            Object wrapped = attributeVariable.getFormatNotNull().processBy(this, new WfVariable(attributeVariable, attributeValue));
            result.put(attributeDefinition.getName(), wrapped);
        }
        return result;
    }

    abstract Map<String, Object> getCustomUserTypeFields(UserTypeFormat userTypeFormat);

    @Override
    public Object onOther(VariableFormat variableFormat, WfVariable variable) {
        return variable.getValue();
    }
}
