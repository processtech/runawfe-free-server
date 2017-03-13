package ru.runa.wfe.var;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.collect.Maps;

public class MapDelegableVariableProvider extends DelegableVariableProvider {
    protected final Map<String, Object> values = Maps.newHashMap();

    public MapDelegableVariableProvider(Map<String, ? extends Object> variables, IVariableProvider delegate) {
        super(delegate);
        for (Map.Entry<String, Object> entry : ((Map<String, Object>) variables).entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public void add(String variableName, Object object) {
        if (object instanceof UserTypeMap) {
            UserTypeMap userTypeMap = (UserTypeMap) object;
            values.put(variableName, new UserTypeMap(userTypeMap.getUserType()));
            Map<String, Object> expanded = userTypeMap.expand(variableName);
            for (Map.Entry<String, Object> entry : expanded.entrySet()) {
                if (entry.getValue() != null) {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
        } else {
            values.put(variableName, object);
        }
    }

    public void add(WfVariable variable) {
        values.put(variable.getDefinition().getName(), variable);
    }

    public Object remove(String variableName) {
        return values.remove(variableName);
    }

    @Override
    public Object getValue(String variableName) {
        Object object;
        if (values.containsKey(variableName)) {
            object = values.get(variableName);
            if (object instanceof WfVariable) {
                return ((WfVariable) object).getValue();
            }
        } else {
            object = super.getValue(variableName);
        }
        mergeLocalValues(variableName, object, null);
        return object;
    }

    @Override
    public WfVariable getVariable(String variableName) {
        if (values.containsKey(variableName)) {
            Object object = getValue(variableName);
            if (object instanceof WfVariable) {
                return (WfVariable) object;
            }
            WfVariable variable = super.getVariable(variableName);
            if (variable != null) {
                log.debug("Setting " + variable + " value to " + object);
                variable.setValue(object);
            }
            return variable;
        }
        WfVariable variable = super.getVariable(variableName);
        if (variable != null) {
            mergeLocalValues(variableName, variable.getValue(), variable.getDefinition());
        }
        return variable;
    }

    private void mergeLocalValues(String variableName, Object object, VariableDefinition variableDefinition) {
        if (object instanceof UserTypeMap) {
            mergeUserTypeVariable(variableName, (UserTypeMap) object);
        } else if (object instanceof List) {
            mergeListVariable(variableName, (List) object, variableDefinition);
        }
    }

    private Object getMergeValue(String variableName) {
        Object componentValue = getValue(variableName);
        if (componentValue instanceof WfVariable) {
            componentValue = ((WfVariable) componentValue).getValue();
        }
        return componentValue;
    }

    private void mergeUserTypeVariable(String variableName, UserTypeMap userTypeMap) {
        for (String componentVariableName : values.keySet()) {
            if (componentVariableName.endsWith(VariableFormatContainer.SIZE_SUFFIX)) {
                continue;
            }
            if (componentVariableName.startsWith(variableName + UserType.DELIM)) {
                String attributeName = componentVariableName.substring(variableName.length() + UserType.DELIM.length());
                Object componentValue = getMergeValue(componentVariableName);
                if (userTypeMap.getUserType().getAttribute(attributeName) == null) {
                    log.debug("Ignored to merge non-defined " + variableName + "." + attributeName + " value " + componentValue);
                    continue;
                }
                log.debug("Merging " + variableName + "." + attributeName + " value to " + componentValue);
                userTypeMap.put(attributeName, componentValue);
            }
        }
    }

    private void mergeListVariable(String variableName, List<Object> list, VariableDefinition variableDefinition) {
        // TODO sneaky code
        if (variableDefinition == null) {
            if (getProcessDefinition() == null) {
                return;
            }
            variableDefinition = getProcessDefinition().getVariable(variableName, false);
            if (variableDefinition == null) {
                // for MultiTask
                return;
            }
        }
        String sizeVariableName = variableName + VariableFormatContainer.SIZE_SUFFIX;
        if (values.containsKey(sizeVariableName)) {
            int size = TypeConversionUtil.convertTo(int.class, values.get(sizeVariableName));
            while (list.size() > size) {
                list.remove(size);
            }
            UserType componentUserType = variableDefinition.getFormatComponentUserTypes()[0];
            for (int i = 0; i < list.size(); i++) {
                String componentVariableName = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                if (componentUserType != null) {
                    mergeUserTypeVariable(componentVariableName, (UserTypeMap) list.get(i));
                } else {
                    Object componentValue = getMergeValue(componentVariableName);
                    list.set(i, componentValue);
                }
            }
            for (int i = list.size(); i < size; i++) {
                String componentVariableName = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                if (componentUserType != null) {
                    UserTypeMap userTypeMap = new UserTypeMap(componentUserType);
                    mergeUserTypeVariable(componentVariableName, userTypeMap);
                    list.add(userTypeMap);
                } else {
                    Object componentValue = getMergeValue(componentVariableName);
                    list.add(componentValue);
                }
            }
        }
    }

    @Override
    public AbstractVariableProvider getSameProvider(Long processId) {
        if (delegate instanceof AbstractVariableProvider) {
            AbstractVariableProvider same = ((AbstractVariableProvider) delegate).getSameProvider(processId);
            return new MapDelegableVariableProvider(values, same);
        }
        return super.getSameProvider(processId);
    }

}
