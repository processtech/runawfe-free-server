package ru.runa.wfe.var.dao;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * All variables must be preloaded and passed to this component.
 *
 * @author AL
 */
public class VariableLoaderFromMap extends VariableLoader {

    /**
     * Preloaded variables. For each process contains map from variable name to variable.
     */
    private final Map<Process, Map<String, Variable>> loadedVariables;

    /**
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     */
    public VariableLoaderFromMap(Map<Process, Map<String, Variable>> loadedVariables) {
        this.loadedVariables = loadedVariables == null ? new HashMap<>() : loadedVariables;
    }

    @Override
    public Variable get(Process process, String name) {
        Map<String, Variable> loadedProcessVariables = loadedVariables.get(process);
        return loadedProcessVariables == null ? null : loadedProcessVariables.get(name);
    }

    @Override
    public Map<String, Object> getAll(Process process) {
        Map<String, Variable> processVariables = loadedVariables.get(process);
        if (processVariables == null) {
            return Maps.newHashMap();
        }
        Map<String, Object> result = Maps.newHashMap();
        for (Variable variable : processVariables.values()) {
            result.put(variable.getName(), variable.getValue());
        }
        return result;
    }
}
