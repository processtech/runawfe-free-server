package ru.runa.wfe.var;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ScriptingUserTypeMap extends UserTypeMap {
    private static final long serialVersionUID = 1L;
    private Set<String> changedAttributeNames = Sets.newHashSet();

    public ScriptingUserTypeMap(UserTypeMap userTypeMap) {
        super(userTypeMap.getUserType());
        for (Map.Entry<String, Object> entry : userTypeMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof UserTypeMap) {
                value = new ScriptingUserTypeMap((UserTypeMap) value);
            }
            super.put(entry.getKey(), value);
        }
    }

    public void markAllAttributesAsChanged() {
        for (Map.Entry<String, Object> entry : entrySet()) {
            changedAttributeNames.add(entry.getKey());
            if (entry.getValue() instanceof ScriptingUserTypeMap) {
                ((ScriptingUserTypeMap) entry.getValue()).markAllAttributesAsChanged();
            }
        }
    }

    public boolean hasChangedAttributes() {
        return changedAttributeNames.size() > 0;
    }

    public Map<String, Object> getChangedVariables(String parentName) {
        Map<String, Object> result = Maps.newHashMap();
        for (String attributeName : changedAttributeNames) {
            Object object = super.get(attributeName);
            String variableName = parentName + UserType.DELIM + attributeName;
            if (!(object instanceof ScriptingUserTypeMap)) {
                result.put(variableName, object);
            }
        }
        for (Map.Entry<String, Object> entry : entrySet()) {
            if (entry.getValue() instanceof ScriptingUserTypeMap) {
                String variableName = parentName + UserType.DELIM + entry.getKey();
                result.putAll(((ScriptingUserTypeMap) entry.getValue()).getChangedVariables(variableName));
            }
        }
        return result;
    }

    @Override
    public Object get(Object key) {
        Object object = super.get(key);
        if (object != null && object.getClass() == UserTypeMap.class) {
            object = new ScriptingUserTypeMap((UserTypeMap) object);
            super.put((String) key, object);
        }
        return object;
    }

    @Override
    public Object put(String key, Object value) {
        String attributeName = key;
        int firstDotIndex = attributeName.indexOf(UserType.DELIM);
        if (firstDotIndex != -1) {
            attributeName = attributeName.substring(0, firstDotIndex);
        }
        attributeName = getUserType().getAttributeNotNull(attributeName).getName();
        changedAttributeNames.add(attributeName);
        // in case of copy user type variable: create new instance
        if (value instanceof UserTypeMap) {
            value = new ScriptingUserTypeMap((UserTypeMap) value);
        }
        // in case of '=' operator
        if (value instanceof ScriptingUserTypeMap) {
            ((ScriptingUserTypeMap) value).markAllAttributesAsChanged();
        }
        return super.put(key, value);
    }

}
