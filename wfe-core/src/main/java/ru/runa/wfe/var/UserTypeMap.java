package ru.runa.wfe.var;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class UserTypeMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;
    private static final Pattern DICT_QUALIFIER = Pattern.compile("(.+)\\[\\s*([^\\]]+)\\]");
    private final UserType userType;

    protected UserTypeMap() {
        this.userType = null;
    }

    public UserTypeMap(UserType userType) {
        this.userType = userType;
        Preconditions.checkNotNull(userType);
    }

    public UserTypeMap(VariableDefinition variableDefinition) {
        this(variableDefinition.getUserType());
    }

    public UserType getUserType() {
        return userType;
    }

    public void merge(Map<?, ?> map, boolean override) {
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            Object targetValue = map.get(attributeDefinition.getName());
            if (Utils.isNullOrEmpty(targetValue) || Objects.equal(targetValue, attributeDefinition.getDefaultValue())) {
                continue;
            }
            Object localValue = get(attributeDefinition.getName());
            if (Objects.equal(localValue, targetValue)) {
                continue;
            }
            if (attributeDefinition.isUserType() && localValue instanceof UserTypeMap) {
                ((UserTypeMap) localValue).merge((Map<?, ?>) targetValue, override);
            } else {
                if (!override && !Utils.isNullOrEmpty(localValue) && !Objects.equal(localValue, attributeDefinition.getDefaultValue())) {
                    continue;
                }
                put(attributeDefinition.getName(), targetValue);
            }
        }
    }

    @Override
    public Object put(String name, Object value) {
        int firstDotIndex = name.indexOf(UserType.DELIM);
        if (firstDotIndex != -1) {
            String attributeName = name.substring(0, firstDotIndex);
            VariableDefinition attributeDefinition = userType.getAttributeNotNull(attributeName);
            if (attributeDefinition.getUserType() == null) {
                throw new InternalApplicationException("Trying to set complex value to non-complex attribute: " + name);
            }
            String nameRemainder = name.substring(firstDotIndex + 1);
            UserTypeMap existingUserTypeMap = (UserTypeMap) get(attributeName);
            super.put(attributeName, existingUserTypeMap);
            return existingUserTypeMap.put(nameRemainder, value);
        } else {
            return super.put(userType.getAttributeNotNull(name).getName(), value);
        }
    }

    @Override
    public Object get(Object key) {
        String attributeName = (String) key;
        int dotIndex = attributeName.indexOf(UserType.DELIM);
        if (dotIndex != -1) {
            String embeddedUserTypeVariable = attributeName.substring(0, dotIndex);
            String embeddedAttributeName = attributeName.substring(dotIndex + 1);
            UserTypeMap embeddedUserTypeMap = (UserTypeMap) get(embeddedUserTypeVariable);
            if (embeddedUserTypeMap != null) {
                return embeddedUserTypeMap.get(embeddedAttributeName);
            }
        }
        VariableDefinition attributeDefinition = userType.getAttribute(attributeName);
        if (attributeDefinition != null) {
            attributeName = attributeDefinition.getName();
        }
        Object object = super.get(attributeName);
        if (object == null && attributeDefinition != null) {
            if (attributeDefinition.getUserType() != null) {
                return buildUserTypeVariable(attributeName, attributeDefinition);
            }
            return attributeDefinition.getDefaultValue();
        }
        return object;
    }

    private UserTypeMap buildUserTypeVariable(String prefix, VariableDefinition variableDefinition) {
        UserTypeMap userTypeMap = new UserTypeMap(variableDefinition);
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String attributeName = prefix + UserType.DELIM + attributeDefinition.getName();
            Object value = super.get(attributeName);
            if (value == null && attributeDefinition.isUserType()) {
                value = buildUserTypeVariable(attributeName, attributeDefinition);
            }
            if (value == null) {
                value = attributeDefinition.getDefaultValue();
            }
            userTypeMap.put(attributeDefinition.getName(), value);
        }
        return userTypeMap;
    }

    public WfVariable getAttributeValue(String attributeName) {
        int dotIndex = attributeName.indexOf(UserType.DELIM);
        if (dotIndex != -1) {
            String embeddedComplexVariable = attributeName.substring(0, dotIndex);
            String embeddedAttributeName = attributeName.substring(dotIndex + 1);
            UserTypeMap embeddedUserTypeMap = (UserTypeMap) super.get(embeddedComplexVariable);
            if (embeddedUserTypeMap != null) {
                return embeddedUserTypeMap.getAttributeValue(embeddedAttributeName);
            } else {
                VariableDefinition variableDefinition = getUserType().getAttributeNotNull(attributeName);
                return new WfVariable(variableDefinition, null);
            }
        }
        String qualifier = null;
        Matcher dictMatcher = DICT_QUALIFIER.matcher(attributeName);
        if (dictMatcher.find()) {
            MatchResult mr = dictMatcher.toMatchResult();
            if (mr.groupCount() == 2) {
                attributeName = mr.group(1).trim();
                qualifier = mr.group(2).trim();
            }
        }
        VariableDefinition variableDefinition = userType.getAttributeNotNull(attributeName);
        Object variableValue = get(attributeName);
        if (qualifier != null) {
            if (ListFormat.class.getName().equals(variableDefinition.getFormatClassName())) {
                VariableFormat qualifierFormat = FormatCommons.createComponent(variableDefinition, 0);
                List<Object> list = (List<Object>) variableValue;
                if (list == null) {
                    return new WfVariable(new VariableDefinition(attributeName, null, qualifierFormat), null);
                }
                Object value = TypeConversionUtil.getListValue(list, Integer.parseInt(qualifier));
                return new WfVariable(new VariableDefinition(attributeName, null, qualifierFormat), value);
            }
            if (MapFormat.class.getName().equals(variableDefinition.getFormatClassName())) {
                if (MapFormat.KEY_NULL_VALUE.equals(qualifier)) {
                    qualifier = "";
                }
                Map<Object, Object> map = (Map<Object, Object>) variableValue;
                VariableFormat qualifierFormat = FormatCommons.createComponent(variableDefinition, 0);
                if (map == null) {
                    return new WfVariable(new VariableDefinition(attributeName, null, qualifierFormat), null);
                }
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    String keyInQualifierFormat = qualifierFormat.format(entry.getKey());
                    if (Objects.equal(keyInQualifierFormat, qualifier) || keyInQualifierFormat == null && Strings.isNullOrEmpty(qualifier)) {
                        Object value = entry.getValue();
                        return new WfVariable(new VariableDefinition(attributeName, null, qualifierFormat), value);
                    }
                }
                throw new IllegalArgumentException("Invalid key = '" + qualifier + "'; all values: " + map);
            }
            throw new IllegalArgumentException("Key '" + qualifier + "' was provided but variable format is "
                    + variableDefinition.getFormatClassName());
        }
        return new WfVariable(variableDefinition, variableValue);
    }

    public Object get(Object key, Object defaultValue) {
        Object value = get(key);
        return value != null ? value : defaultValue;
    }

    public Map<String, Object> expand(String prefix) {
        Map<String, Object> result = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : entrySet()) {
            String name = prefix + UserType.DELIM + entry.getKey();
            if (entry.getValue() instanceof UserTypeMap) {
                result.putAll(((UserTypeMap) entry.getValue()).expand(name));
            } else {
                result.put(name, entry.getValue());
            }
        }
        return result;
    }

    public Map<VariableDefinition, Object> expandAttributes(String prefix) {
        Map<VariableDefinition, Object> result = Maps.newHashMap();
        for (Map.Entry<String, Object> entry : entrySet()) {
            String name = prefix + UserType.DELIM + entry.getKey();
            if (entry.getValue() instanceof UserTypeMap) {
                result.putAll(((UserTypeMap) entry.getValue()).expandAttributes(name));
            } else {
                VariableDefinition definition;
                VariableDefinition attributeDefinition = userType.getAttribute(entry.getKey());
                if (attributeDefinition != null) {
                    definition = new VariableDefinition(name, null, attributeDefinition);
                } else {
                    definition = new VariableDefinition(name, null);
                }
                result.put(definition, entry.getValue());
            }
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            Object object = get(attributeDefinition.getName());
            if (!Utils.isNullOrEmpty(object) && !Objects.equal(object, attributeDefinition.getDefaultValue())) {
                return false;
            }
        }
        return true;
    }
}
