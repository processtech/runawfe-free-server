package ru.runa.wfe.service.utils;

import java.util.List;
import java.util.Map;

import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.logic.VariableLogic;

public class FileVariablesUtil {
    private static final VariableLogic variableLogic = ApplicationContextFactory.getVariableLogic();

    public static void proxyFileVariables(User user, Long processId, WfVariable variable) {
        if (variable == null) {
            return;
        }
        variable.setValue(proxyFileVariableValues(user, processId, variable.getDefinition().getName(), variable.getValue()));
    }

    private static Object proxyFileVariableValues(User user, Long processId, String variableName, Object variableValue) {
        if (variableValue instanceof FileVariable) {
            FileVariable fileVariable = (FileVariable) variableValue;
            return new FileVariableProxy(user, processId, variableName, fileVariable);
        }
        if (variableValue instanceof List) {
            @SuppressWarnings("unchecked")
            val list = (List<Object>) variableValue;
            int i = 0;
            for (val object : list) {
                if (object instanceof FileVariable || object instanceof List || object instanceof Map) {
                    String proxyName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + i +
                            VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    Object proxy = proxyFileVariableValues(user, processId, proxyName, object);
                    if (object instanceof FileVariable) {
                        TypeConversionUtil.setListValue(variableValue, i, proxy);
                    }
                }
                i++;
            }
        }
        if (variableValue instanceof Map) {
            @SuppressWarnings("unchecked")
            val map = (Map<?, Object>) variableValue;
            for (val entry : map.entrySet()) {
                val object = entry.getValue();
                if (object instanceof FileVariable || object instanceof List || object instanceof Map) {
                    String proxyName = map instanceof UserTypeMap
                            ? variableName + UserType.DELIM + entry.getKey()
                            : variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + entry.getKey() +
                              VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    Object proxy = proxyFileVariableValues(user, processId, proxyName, object);
                    if (object instanceof FileVariable) {
                        entry.setValue(proxy);
                    }
                }
            }
        }
        return variableValue;
    }

    public static void unproxyFileVariables(User user, Long processId, Long taskId, Map<String, Object> variables) {
        unproxyFileVariableValues(user, processId, taskId, variables);
    }

    private static Object unproxyFileVariableValues(User user, Long processId, Long taskId, Object variableValue) {
        if (variableValue instanceof FileVariableProxy) {
            FileVariableProxy proxy = (FileVariableProxy) variableValue;
            if (proxy.getUnproxiedClassName() != null) {
                return ClassLoaderUtil.instantiate(proxy.getUnproxiedClassName(), proxy.getStringValue());
            } else {
                WfVariable variable;
                if (taskId != null) {
                    variable = variableLogic.getTaskVariable(user, processId, taskId, proxy.getVariableName());
                } else {
                    variable = variableLogic.getVariable(user, processId, proxy.getVariableName());
                }
                if (variable == null || variable.getValue() == null) {
                    throw new InternalApplicationException("FileVariableProxy provided for null variable " + proxy.getVariableName());
                }
                if (variable.getValue() instanceof FileVariable) {
                    return variable.getValue();
                }
                throw new InternalApplicationException("FileVariableProxy provided for non-file " + variable);
            }
        }
        if (variableValue instanceof List) {
            @SuppressWarnings("unchecked")
            val list = (List<Object>) variableValue;
            int i = 0;
            for (val object : list) {
                if (object instanceof FileVariableProxy || object instanceof List || object instanceof Map) {
                    Object unproxied = unproxyFileVariableValues(user, processId, taskId, object);
                    if (object instanceof FileVariable) {
                        TypeConversionUtil.setListValue(variableValue, i, unproxied);
                    }
                }
                i++;
            }
        }
        if (variableValue instanceof Map) {
            @SuppressWarnings("unchecked")
            val map = (Map<?, Object>) variableValue;
            for (val entry : map.entrySet()) {
                val object = entry.getValue();
                if (object instanceof FileVariableProxy || object instanceof List || object instanceof Map) {
                    Object unproxied = unproxyFileVariableValues(user, processId, taskId, object);
                    if (object instanceof FileVariable) {
                        entry.setValue(unproxied);
                    }
                }
            }
        }
        return variableValue;
    }
}
