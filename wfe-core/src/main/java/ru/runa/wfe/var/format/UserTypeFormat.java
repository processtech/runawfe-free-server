package ru.runa.wfe.var.format;

import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;

public class UserTypeFormat extends VariableFormat implements VariableDisplaySupport {
    private static final Log log = LogFactory.getLog(UserTypeFormat.class);
    private final UserType userType;

    public UserTypeFormat(UserType userType) {
        Preconditions.checkNotNull(userType);
        this.userType = userType;
    }

    @Override
    public Class<?> getJavaClass() {
        return UserTypeMap.class;
    }

    @Override
    public String getName() {
        return JSONValue.toJSONString(buildFormatDescriptor(userType));
    }

    private static Map<String, Object> buildFormatDescriptor(UserType userType) {
        Map<String, Object> map = Maps.newLinkedHashMap();
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            Object value;
            if (attributeDefinition.isUserType()) {
                value = buildFormatDescriptor(attributeDefinition.getUserType());
            } else {
                value = FormatCommons.create(attributeDefinition).getName();
            }
            map.put(attributeDefinition.getName(), value);
        }
        return map;
    }

    @Override
    protected UserTypeMap convertFromStringValue(String source) {
        return convertFromJSONValue(JSONValue.parse(source.replaceAll("&quot;", "\"")));
    }

    @Override
    protected String convertToStringValue(Object obj) {
        return JSONValue.toJSONString(convertToJSONValue(obj));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected UserTypeMap convertFromJSONValue(Object jsonValue) {
        JSONObject object = (JSONObject) jsonValue;
        UserTypeMap result = new UserTypeMap(userType);
        for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) object.entrySet()) {
            VariableDefinition attributeDefinition = userType.getAttributeNotNull(entry.getKey());
            try {
                VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
                Object attributeValue = attributeFormat.convertFromJSONValue(entry.getValue());
                result.put(attributeDefinition.getName(), attributeValue);
            } catch (Exception e) {
                log.error("Near " + attributeDefinition.toString() + ": " + e);
                Throwables.propagate(e);
            }
        }
        return result;
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        UserTypeMap userTypeMap = (UserTypeMap) value;
        Map<Object, Object> map = Maps.newLinkedHashMap();
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
            Object attributeValue = userTypeMap.get(attributeDefinition.getName());
            if (attributeValue != null) {
                attributeValue = TypeConversionUtil.convertTo(attributeFormat.getJavaClass(), attributeValue);
                attributeValue = attributeFormat.convertToJSONValue(attributeValue);
                map.put(attributeDefinition.getName(), attributeValue);
            }
        }
        return map;
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long processId, String name, Object object) {
        UserTypeMap userTypeMap = (UserTypeMap) object;
        StringBuffer b = new StringBuffer();
        b.append("<table class=\"list usertype\">");
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            b.append("<tr>");
            b.append("<td class=\"list\">").append(attributeDefinition.getName()).append("</td>");
            VariableFormat attributeFormat = FormatCommons.create(attributeDefinition);
            Object attributeValue = userTypeMap.get(attributeDefinition.getName());
            b.append("<td class=\"list\">");
            if (attributeValue != null) {
                Object value;
                if (attributeFormat instanceof VariableDisplaySupport) {
                    String childName = name + UserType.DELIM + attributeDefinition.getName();
                    try {
                        value = ((VariableDisplaySupport) attributeFormat).formatHtml(user, webHelper, processId, childName, attributeValue);
                    } catch (Exception e) {
                        throw new InternalApplicationException(attributeDefinition.getName(), e);
                    }
                } else {
                    value = attributeFormat.format(attributeValue);
                }
                b.append(value);
            }
            b.append("</td>");
            b.append("</tr>");
        }
        b.append("</table>");
        return b.toString();
    }

    public UserType getUserType() {
        return userType;
    }

    @Override
    public String toString() {
        return userType.getName();
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onUserType(this, context);
    }
}