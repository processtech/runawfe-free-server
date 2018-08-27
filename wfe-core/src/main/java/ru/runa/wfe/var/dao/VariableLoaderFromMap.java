package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Maps;

/**
 * All variables must be preloaded and passed to this component.
 *
 * @author AL
 */
public class VariableLoaderFromMap extends AbstractVariableLoader {

    /**
     * Preloaded variables. For each process contains map from variable name to variable. If no entry for variable name exists in preloaded variables,
     * then it will be loaded via {@link CurrentVariableDao}.
     */
    private final Map<CurrentProcess, Map<String, CurrentVariable<?>>> loadedVariables;

    /**
     * Supports variable loading via {@link CurrentVariableDao} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component
     * in case of mass variables loading.
     *
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     */
    public VariableLoaderFromMap(Map<CurrentProcess, Map<String, CurrentVariable<?>>> loadedVariables) {
        this.loadedVariables = loadedVariables == null ? new HashMap<CurrentProcess, Map<String, CurrentVariable<?>>>() : loadedVariables;
    }

    @Override
    public CurrentVariable<?> get(CurrentProcess process, String name) {
        Map<String, CurrentVariable<?>> loadedProcessVariables = loadedVariables.get(process);
        if (loadedProcessVariables == null || !loadedProcessVariables.containsKey(name)) {
            return null;
        }
        return loadedProcessVariables.get(name);
    }

    @Override
    public List<CurrentVariable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        return null;
    }

    @Override
    public Map<String, Object> getAll(CurrentProcess process) {
        Map<String, CurrentVariable<?>> processVariables = loadedVariables.get(process);
        if (processVariables == null) {
            return Maps.newHashMap();
        }
        Map<String, Object> result = Maps.newHashMap();
        for (CurrentVariable<?> variable : processVariables.values()) {
            result.put(variable.getName(), variable.getValue());
        }
        return result;
    }
}
