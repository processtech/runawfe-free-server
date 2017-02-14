package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import ru.runa.wfe.var.legacy.ComplexVariable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Supports variable loading via {@link VariableDAO} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component in
 * case of mass variables loading.
 *
 * @author AL
 */
@SuppressWarnings({ "unchecked" })
public class VariableLoader {
    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(VariableLoader.class);

    /**
     * {@link VariableDAO} for loading variables if no preloaded variable is available.
     */
    private final VariableDAO dao;

    /**
     * Preloaded variables. For each process contains map from variable name to variable. If no entry for variable name exists in preloaded variables,
     * then it will be loaded via {@link VariableDAO}.
     */
    private final Map<Process, Map<String, Variable<?>>> loadedVariables;

    /**
     * Flag, equals true, if loading from dao is disabled (return null) and false for loading variable from dao if it absent in loadedVariables.
     */
    private boolean disableDaoFallback;

    /**
     * Supports variable loading via {@link VariableDAO} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component
     * in case of mass variables loading.
     *
     * @param dao
     *            {@link VariableDAO} for loading variables if no preloaded variable is available.
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     * @param disableDaoFallback
     *            Flag, equals true, if loading from dao is disabled (return null) and false for loading variable from dao if it absent in
     *            loadedVariables.
     */
    public VariableLoader(VariableDAO dao, Map<Process, Map<String, Variable<?>>> loadedVariables, boolean disableDaoFallback) {
        this.dao = dao;
        this.loadedVariables = loadedVariables == null ? new HashMap<Process, Map<String, Variable<?>>>() : loadedVariables;
        this.disableDaoFallback = disableDaoFallback;
    }

    /**
     * Get variable with given name for process.
     *
     * @param process
     *            Process, which variable must be loaded.
     * @param name
     *            Variable name.
     * @return Variable or null, if no variable found.
     */
    public Variable<?> get(Process process, String name) {
        Map<String, Variable<?>> loadedProcessVariables = loadedVariables.get(process);
        if (disableDaoFallback || loadedProcessVariables != null && loadedProcessVariables.containsKey(name)) {
            if (loadedProcessVariables != null) {
                return loadedProcessVariables.get(name);
            }
            return null;
        }
        return dao.get(process, name);
    }

    public WfVariable getVariable(ProcessDefinition processDefinition, Process process, String variableName) {
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, false);
        if (variableDefinition != null) {
            Object variableValue = getVariableValue(processDefinition, process, variableDefinition);
            if (variableValue == null && SystemProperties.isV4ListVariableCompatibilityMode()
                    && variableName.endsWith(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                String listVariableName = variableName.substring(0, variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START));
                int listIndex = Integer.parseInt(variableName.substring(variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START) + 1,
                        variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_END)));
                VariableDefinition listVariableDefinition = processDefinition.getVariable(listVariableName, false);
                List<Object> list = (List<Object>) getVariableValue(processDefinition, process, listVariableDefinition);
                if (list != null) {
                    if (list.size() > listIndex) {
                        variableValue = list.get(listIndex);
                    } else {
                        log.warn("Strange list when requesting " + process + ":" + variableName + ": " + list);
                    }
                }
            }
            return new WfVariable(variableDefinition, variableValue);
        }
        if (SystemProperties.isV3CompatibilityMode() || SystemProperties.isAllowedNotDefinedVariables()) {
            Variable<?> variable = get(process, variableName);
            return new WfVariable(variableName, variable != null ? variable.getValue() : null);
        }
        log.debug("No variable defined by name '" + variableName + "' in " + process + ", returning null");
        return null;
    }

    public Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition) {
        if (variableDefinition.isUserType()) {
            return loadUserTypeVariable(processDefinition, process, variableDefinition.getName(), variableDefinition);
        } else if (ListFormat.class.getName().equals(variableDefinition.getFormatClassName())) {
            return loadListVariable(processDefinition, process, variableDefinition);
        } else {
            Variable<?> variable = get(process, variableDefinition.getName());
            if (variable != null) {
                Object value = variable.getValue();
                value = processComplexVariablesPre430(processDefinition, variableDefinition, variableDefinition.getUserType(), value);
                return value;
            }
            return variableDefinition.getDefaultValue();
        }
    }

    public static Object processComplexVariablesPre430(ProcessDefinition processDefinition, VariableDefinition variableDefinition, UserType userType,
            Object value) {
        if (value instanceof ComplexVariable) {
            UserTypeMap map = new UserTypeMap(userType);
            // limitation: embedded complex variables
            map.putAll((ComplexVariable) value);
            return map;
        }
        if (value instanceof UserTypeMap) {
            return value;
        }
        if (value instanceof Map) {
            Preconditions.checkNotNull(variableDefinition, "embedded containers does not supported now");
            Map<Object, Object> map = (Map<Object, Object>) value;
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                if (variableDefinition.getFormatComponentUserTypes()[0] != null) {
                    map.put(entry.getKey(),
                            processComplexVariablesPre430(processDefinition, null, variableDefinition.getFormatComponentUserTypes()[0],
                                    entry.getValue()));
                }
                if (variableDefinition.getFormatComponentUserTypes()[1] != null) {
                    map.put(entry.getKey(),
                            processComplexVariablesPre430(processDefinition, null, variableDefinition.getFormatComponentUserTypes()[1],
                                    entry.getValue()));
                }
            }
        }
        if (value instanceof List) {
            Preconditions.checkNotNull(variableDefinition, "embedded containers does not supported now");
            List<Object> list = (List<Object>) value;
            for (int i = 0; i < list.size(); i++) {
                if (variableDefinition.getFormatComponentUserTypes()[0] != null) {
                    list.set(i,
                            processComplexVariablesPre430(processDefinition, null, variableDefinition.getFormatComponentUserTypes()[0], list.get(i)));
                }
            }
        }
        return value;
    }

    private UserTypeMap loadUserTypeVariable(ProcessDefinition processDefinition, Process process, String prefix,
            VariableDefinition variableDefinition) {
        UserTypeMap userTypeMap = new UserTypeMap(variableDefinition);
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String fullName = prefix + UserType.DELIM + attributeDefinition.getName();
            VariableDefinition definition = new VariableDefinition(fullName, null, attributeDefinition);
            Object value = getVariableValue(processDefinition, process, definition);
            userTypeMap.put(attributeDefinition.getName(), value);
        }
        return userTypeMap;
    }

    private List<Object> loadListVariable(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition) {
        List<Object> list = Lists.newArrayList();
        String sizeVariableName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
        VariableDefinition sizeDefinition = new VariableDefinition(sizeVariableName, null, LongFormat.class.getName(), null);
        Integer size = (Integer) getVariableValue(processDefinition, process, sizeDefinition);
        if (size == null && SystemProperties.isV4ListVariableCompatibilityMode()) {
            Variable<?> variable = get(process, variableDefinition.getName());
            if (variable != null && variable.getValue() instanceof List) {
                return (List<Object>) processComplexVariablesPre430(processDefinition, variableDefinition, null, variable.getValue());
            }
            return null;
        }
        String[] formatComponentClassNames = variableDefinition.getFormatComponentClassNames();
        String componentFormat = formatComponentClassNames.length > 0 ? formatComponentClassNames[0] : null;
        UserType[] formatComponentUserTypes = variableDefinition.getFormatComponentUserTypes();
        UserType componentUserType = formatComponentUserTypes.length > 0 ? formatComponentUserTypes[0] : null;
        for (int i = 0; i < size; i++) {
            String componentName = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END;
            VariableDefinition componentDefinition = new VariableDefinition(componentName, null, componentFormat, componentUserType);
            Object componentValue = getVariableValue(processDefinition, process, componentDefinition);
            list.add(componentValue);
        }
        return list;
    }
}
