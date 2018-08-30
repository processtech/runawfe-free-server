package ru.runa.wfe.var.dao;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.dto.WfVariable;

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
    private final Map<Process, Map<String, BaseVariable>> loadedVariables;

    /**
     * Supports variable loading via {@link CurrentVariableDao} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component
     * in case of mass variables loading.
     *
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     */
    public VariableLoaderFromMap(Map<Process, Map<String, BaseVariable>> loadedVariables) {
        this.loadedVariables = loadedVariables == null ? new HashMap<>() : loadedVariables;
    }

    @Override
    public BaseVariable get(Process process, String name) {
        Map<String, BaseVariable> loadedProcessVariables = loadedVariables.get(process);
        return loadedProcessVariables != null && loadedProcessVariables.containsKey(name)
                ? loadedProcessVariables.get(name)
                : null;
    }

    @Override
    public Map<String, Object> getAll(Process process) {
        Map<String, BaseVariable> processVariables = loadedVariables.get(process);
        if (processVariables == null) {
            return Maps.newHashMap();
        }
        Map<String, Object> result = Maps.newHashMap();
        for (BaseVariable variable : processVariables.values()) {
            result.put(variable.getName(), variable.getValue());
        }
        return result;
    }
}
