package ru.runa.wfe.service.utils;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.logic.VariableLogic;

public class FileVariablesUtil {
    private static final VariableLogic variableLogic = ApplicationContextFactory.getVariableLogic();

    public static void proxyFileVariables(User user, Long processId, WfVariable variable) {
        proxyFileVariables(user, processId, null, variable);
    }

    public static void proxyFileVariables(User user, Long processId, Long definitionId, WfVariable variable) {
        if (variable == null) {
            return;
        }
        variable.setValue(proxyFileVariableValues(user, processId, definitionId, variable.getDefinition().getName(), variable.getValue()));
    }

    public static Object proxyFileVariableValues(User user, Long processId, String variableName, Object variableValue) {
        return proxyFileVariableValues(user, processId, null, variableName, variableValue);
    }

    public static Object proxyFileVariableValues(User user, Long processId, Long definitionId, String variableName, Object variableValue) {
        if (variableValue instanceof FileVariable) {
            FileVariable fileVariable = (FileVariable) variableValue;
            return new FileVariableProxy(user, processId, definitionId, variableName, fileVariable);
        }
        if (variableValue instanceof List) {
            for (int i = 0; i < TypeConversionUtil.getListSize(variableValue); i++) {
                Object object = TypeConversionUtil.getListValue(variableValue, i);
                if (object instanceof FileVariable || object instanceof List || object instanceof Map) {
                    String proxyName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    Object proxy = proxyFileVariableValues(user, processId, definitionId, proxyName, object);
                    if (object instanceof FileVariable) {
                        TypeConversionUtil.setListValue(variableValue, i, proxy);
                    }
                }
            }
        }
        if (variableValue instanceof Map) {
            Map<?, Object> map = (Map<?, Object>) variableValue;
            for (Map.Entry<?, Object> entry : map.entrySet()) {
                Object object = entry.getValue();
                if (object instanceof FileVariable || object instanceof List || object instanceof Map) {
                    String proxyName;
                    if (map instanceof UserTypeMap) {
                        proxyName = variableName + UserType.DELIM + entry.getKey();
                    } else {
                        proxyName = variableName + VariableFormatContainer.COMPONENT_QUALIFIER_START + entry.getKey()
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    }
                    Object proxy = proxyFileVariableValues(user, processId, definitionId, proxyName, object);
                    if (object instanceof FileVariable) {
                        entry.setValue(proxy);
                    }
                }
            }
        }
        return variableValue;
    }

    public static void unproxyFileVariables(User user, Long processId, Long taskId, Map<String, Object> variables) {
        unproxyFileVariableValues(user, processId, null, taskId, variables);
    }

    public static void unproxyFileVariables(User user, Long definitionId, Map<String, Object> variables) {
        unproxyFileVariableValues(user, null, definitionId, null, variables);
    }

    private static Object unproxyFileVariableValues(User user, Long processId, Long definitionId, Long taskId, Object variableValue) {
        if (variableValue instanceof FileVariableProxy) {
            FileVariableProxy proxy = (FileVariableProxy) variableValue;
            if (proxy.getUnproxiedClassName() != null) {
                return ClassLoaderUtil.instantiate(proxy.getUnproxiedClassName(), proxy.getStringValue());
            } else {
                WfVariable variable;
                if (taskId != null) {
                    variable = variableLogic.getTaskVariable(user, processId, taskId, proxy.getVariableName());
                } else if (processId != null) {
                    variable = variableLogic.getVariable(user, processId, proxy.getVariableName());
                } else {
                    if (definitionId == null) {
                        throw new IllegalStateException("One of processId, definitionId, taskId should not be null");
                    }
                    variable = variableLogic.getVariableDefaultValue(user, definitionId, proxy.getVariableName());
                }
                if (variable == null) {
                    throw new InternalApplicationException("FileVariableProxy provided for non-existing variable " + proxy.getVariableName());
                }
                if (!(variable.getDefinition().getFormatNotNull() instanceof FileFormat)) {
                    throw new InternalApplicationException("FileVariableProxy provided for non-file " + variable);
                }
                return variable.getValue();
            }
        }
        if (variableValue instanceof List) {
            for (int i = 0; i < TypeConversionUtil.getListSize(variableValue); i++) {
                Object object = TypeConversionUtil.getListValue(variableValue, i);
                if (object instanceof FileVariableProxy || object instanceof List || object instanceof Map) {
                    Object unproxied = unproxyFileVariableValues(user, processId, definitionId, taskId, object);
                    if (object instanceof FileVariable) {
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
                    Object unproxied = unproxyFileVariableValues(user, processId, definitionId, taskId, object);
                    if (object instanceof FileVariable) {
                        entry.setValue(unproxied);
                    }
                }
            }
        }
        return variableValue;
    }

}
