package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Supports variable loading via {@link VariableDao} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component in
 * case of mass variables loading.
 *
 * @author AL
 */
@SuppressWarnings({ "unchecked" })
public class VariableLoaderDaoFallback extends VariableLoader {

    /**
     * {@link VariableDao} for loading variables if no preloaded variable is available.
     */
    private final VariableDao dao;

    /**
     * Preloaded variables. For each process contains map from variable name to variable. If no entry for variable name exists in preloaded variables,
     * then it will be loaded via {@link VariableDao}.
     */
    private final Map<Process, Map<String, Variable>> loadedVariables;

    /**
     * Supports variable loading via {@link VariableDao} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component
     * in case of mass variables loading.
     *
     * @param dao
     *            {@link VariableDao} for loading variables if no preloaded variable is available.
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     */
    public VariableLoaderDaoFallback(VariableDao dao, Map<Process, Map<String, Variable>> loadedVariables) {
        this.dao = dao;
        this.loadedVariables = loadedVariables == null ? new HashMap<>() : loadedVariables;
    }

    @Override
    public Variable get(Process process, String name) {
        Map<String, Variable> loadedProcessVariables = loadedVariables.get(process);
        return loadedProcessVariables != null && loadedProcessVariables.containsKey(name)
                ? loadedProcessVariables.get(name)
                : dao.get(process, name);
    }

    @Override
    public Map<String, Object> getAll(Process process) {
        return dao.getAll(process);
    }
}
