package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * All variables must be preloaded and passed to this component.
 *
 * @author AL
 */
@SuppressWarnings({ "unchecked" })
public class VariableLoaderFromMap implements VariableLoader {

    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(VariableLoaderFromMap.class);

    /**
     * Preloaded variables. For each process contains map from variable name to variable. If no entry for variable name exists in preloaded variables,
     * then it will be loaded via {@link VariableDAO}.
     */
    private final Map<Process, Map<String, Variable<?>>> loadedVariables;

    /**
     * Supports variable loading via {@link VariableDAO} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component
     * in case of mass variables loading.
     *
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     */
    public VariableLoaderFromMap(Map<Process, Map<String, Variable<?>>> loadedVariables) {
        super();
        this.loadedVariables = loadedVariables == null ? new HashMap<Process, Map<String, Variable<?>>>() : loadedVariables;
    }

    @Override
    public Variable<?> get(Process process, String name) {
        Map<String, Variable<?>> loadedProcessVariables = loadedVariables.get(process);
        if (loadedProcessVariables == null || !loadedProcessVariables.containsKey(name)) {
            return null;
        }
        return loadedProcessVariables.get(name);
    }

    @Override
    public List<Variable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        return null;
    }

    @Override
    public Map<String, Object> getAll(Process process) {
        Map<String, Variable<?>> processVariables = loadedVariables.get(process);
        if (processVariables == null) {
            return Maps.newHashMap();
        }
        Map<String, Object> result = Maps.newHashMap();
        for (Variable<?> variable : processVariables.values()) {
            result.put(variable.getName(), variable.getValue());
        }
        return result;
    }

    @Override
    public WfVariable getVariable(ProcessDefinition processDefinition, Process process, String variableName) {
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, false);
        if (variableDefinition != null) {
            Object variableValue = getVariableValue(processDefinition, process, variableDefinition);
            if (variableValue == null && SystemProperties.isV4ListVariableCompatibilityMode() && variableName.endsWith(
                    VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
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
        if (SystemProperties.isV3CompatibilityMode()) {
            Variable<?> variable = get(process, variableName);
            return new WfVariable(variableName, variable != null ? variable.getValue() : null);
        }
        log.debug("No variable defined by name '" + variableName + "' in " + process + ", returning null");
        return null;
    }

    @Override
    public Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition) {
        return variableDefinition.getFormatNotNull().processBy(new LoadVariableOfType(), new LoadVariableOfTypeContext(processDefinition, process,
                this, variableDefinition));
    }
}
