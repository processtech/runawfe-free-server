package ru.runa.wfe.service.utils;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.IFileVariable;
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

    public static Object proxyFileVariableValues(User user, Long processId, String variableName, Object variableValue) {
        if (variableValue instanceof IFileVariable) {
            IFileVariable fileVariable = (IFileVariable) variableValue;
            return new FileVariableProxy(user, processId, variableName, fileVariable);
        }
        if (variableValue instanceof List) {
            for (int i = 0; i < TypeConversionUtil.getListSize(variableValue); i++) {
                Object object = TypeConversionUtil.getListValue(variableValue, i);
                if (object instanceof IFileVariable || object instanceof List || object instanceof Map) {
                    String proxyName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    Object proxy = proxyFileVariableValues(user, processId, proxyName, object);
                    if (object instanceof IFileVariable) {
                        TypeConversionUtil.setListValue(variableValue, i, proxy);
                    }
                }
            }
        }
        if (variableValue instanceof Map) {
            Map<?, Object> map = (Map<?, Object>) variableValue;
            for (Map.Entry<?, Object> entry : map.entrySet()) {
                Object object = entry.getValue();
                if (object instanceof IFileVariable || object instanceof List || object instanceof Map) {
                    String proxyName;
                    if (map instanceof UserTypeMap) {
                        proxyName = variableName + UserType.DELIM + entry.getKey();
                    } else {
                        proxyName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + entry.getKey()
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    }
                    Object proxy = proxyFileVariableValues(user, processId, proxyName, object);
                    if (object instanceof IFileVariable) {
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
                if (variable.getValue() instanceof IFileVariable) {
                    return variable.getValue();
                }
                throw new InternalApplicationException("FileVariableProxy provided for non-file " + variable);
            }
        }
        if (variableValue instanceof List) {
            for (int i = 0; i < TypeConversionUtil.getListSize(variableValue); i++) {
                Object object = TypeConversionUtil.getListValue(variableValue, i);
                if (object instanceof FileVariableProxy || object instanceof List || object instanceof Map) {
                    Object unproxied = unproxyFileVariableValues(user, processId, taskId, object);
                    if (object instanceof IFileVariable) {
                        TypeConversionUtil.setListValue(variableValue, i, unproxied);
                    }
                }
            }
        }
        if (variableValue instanceof Map) {
            Map<?, Object> map = (Map<?, Object>) variableValue;
            for (Map.Entry<?, Object> entry : map.entrySet()) {
                Object object = entry.getValue();
                if (object instanceof FileVariableProxy || object instanceof List || object instanceof Map) {
                    Object unproxied = unproxyFileVariableValues(user, processId, taskId, object);
                    if (object instanceof IFileVariable) {
                        entry.setValue(unproxied);
                    }
                }
            }
        }
        return variableValue;
    }

}
