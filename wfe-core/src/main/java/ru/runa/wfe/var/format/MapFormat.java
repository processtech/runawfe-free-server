package ru.runa.wfe.var.format;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import org.json.simple.JSONObject;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;

/**
 * @deprecated since 4.6.0 rm#3254
 */
@Deprecated
@SuppressWarnings({ "unchecked", "rawtypes" })
public class MapFormat extends VariableFormat implements VariableFormatContainer, VariableDisplaySupport {
    public static final String KEY_NULL_VALUE = "null";
    private String keyFormatClassName;
    private UserType keyUserType;
    private String valueFormatClassName;
    private UserType valueUserType;

    @Override
    public Class<?> getJavaClass() {
        return Map.class;
    }

    @Override
    public String getName() {
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        return "map(" + keyFormat.getName() + ", " + valueFormat.getName() + ")";
    }

    @Override
    public String getComponentClassName(int index) {
        if (index == 0) {
            return keyFormatClassName;
        }
        if (index == 1) {
            return valueFormatClassName;
        }
        return null;
    }

    @Override
    public void setComponentClassNames(String[] componentClassNames) {
        if (componentClassNames.length == 2 && componentClassNames[0] != null && componentClassNames[1] != null) {
            keyFormatClassName = componentClassNames[0];
            valueFormatClassName = componentClassNames[1];
        } else {
            keyFormatClassName = StringFormat.class.getName();
            valueFormatClassName = StringFormat.class.getName();
        }
    }

    @Override
    public UserType getComponentUserType(int index) {
        if (index == 0) {
            return keyUserType;
        }
        if (index == 1) {
            return valueUserType;
        }
        return null;
    }

    @Override
    public void setComponentUserTypes(UserType[] componentUserTypes) {
        if (componentUserTypes.length == 2) {
            keyUserType = componentUserTypes[0];
            valueUserType = componentUserTypes[1];
        }
    }

    @Override
    public Map<?, ?> convertFromStringValue(String json) throws Exception {
        return (Map<?, ?>) parseJSON(json);
    }

    @Override
    public String convertToStringValue(Object object) {
        return formatJSON(object);
    }

    @Override
    public Map<?, ?> convertFromJSONValue(Object json) {
        JSONObject object = (JSONObject) json;
        Map result = Maps.newHashMapWithExpectedSize(object.size());
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : (Set<Map.Entry<Object, Object>>) object.entrySet()) {
            Object key = keyFormat.convertFromJSONValue(entry.getKey());
            Object value = valueFormat.convertFromJSONValue(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    @Override
    public Object convertToJSONValue(Object map) {
        JSONObject object = new JSONObject();
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) map).entrySet()) {
            Object keyValue = TypeConversionUtil.convertTo(keyFormat.getJavaClass(), entry.getKey());
            Object valueValue = TypeConversionUtil.convertTo(valueFormat.getJavaClass(), entry.getValue());
            object.put(keyFormat.convertToJSONValue(keyValue), valueFormat.convertToJSONValue(valueValue));
        }
        return object;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object map) {
        if (map == null) {
            return "";
        }
        StringBuffer b = new StringBuffer();
        b.append("<table class=\"list\">");
        VariableFormat keyFormat = FormatCommons.createComponent(this, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(this, 1);
        for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) map).entrySet()) {
            b.append("<tr><td class=\"list\">");
            String value;
            Object keyValue = TypeConversionUtil.convertTo(keyFormat.getJavaClass(), entry.getKey());
            if (keyFormat instanceof VariableDisplaySupport) {
                value = ((VariableDisplaySupport) keyFormat).formatHtml(user, webHelper, processId, name, keyValue);
            } else {
                value = keyFormat.format(keyValue);
            }
            b.append(value);
            b.append("</td><td class=\"list\">");
            Object valueValue = TypeConversionUtil.convertTo(valueFormat.getJavaClass(), entry.getValue());
            if (valueFormat instanceof VariableDisplaySupport) {
                String componentName = name + COMPONENT_QUALIFIER_START + keyFormat.format(keyValue) + COMPONENT_QUALIFIER_END;
                value = ((VariableDisplaySupport) valueFormat).formatHtml(user, webHelper, processId, componentName, valueValue);
            } else {
                value = valueFormat.format(valueValue);
            }
            b.append(value);
            b.append("</td></tr>");
        }
        b.append("</table>");
        return b.toString();
    }

    @Override
    public String toString() {
        return getClass().getName() + COMPONENT_PARAMETERS_START + keyFormatClassName + COMPONENT_PARAMETERS_DELIM + valueFormatClassName
                + COMPONENT_PARAMETERS_END;
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onMap(this, context);
    }

    @Override
    public boolean canBePersistedAsComplexVariable() {
        return true;
    }

}
