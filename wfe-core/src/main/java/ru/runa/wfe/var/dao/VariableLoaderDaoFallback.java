package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Supports variable loading via {@link CurrentVariableDao} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component in
 * case of mass variables loading.
 *
 * @author AL
 */
@SuppressWarnings({ "unchecked" })
public class VariableLoaderDaoFallback extends AbstractVariableLoader {

    /**
     * {@link CurrentVariableDao} for loading variables if no preloaded variable is available.
     */
    private final CurrentVariableDao dao;

    /**
     * Preloaded variables. For each process contains map from variable name to variable. If no entry for variable name exists in preloaded variables,
     * then it will be loaded via {@link CurrentVariableDao}.
     */
    private final Map<CurrentProcess, Map<String, CurrentVariable<?>>> loadedVariables;

    /**
     * Supports variable loading via {@link CurrentVariableDao} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component
     * in case of mass variables loading.
     *
     * @param dao
     *            {@link CurrentVariableDao} for loading variables if no preloaded variable is available.
     * @param loadedVariables
     *            Preloaded variables. For each process contains map from variable name to variable. May be null.
     */
    public VariableLoaderDaoFallback(CurrentVariableDao dao, Map<CurrentProcess, Map<String, CurrentVariable<?>>> loadedVariables) {
        this.dao = dao;
        this.loadedVariables = loadedVariables == null ? new HashMap<CurrentProcess, Map<String, CurrentVariable<?>>>() : loadedVariables;
    }

    @Override
    public CurrentVariable<?> get(CurrentProcess process, String name) {
        Map<String, CurrentVariable<?>> loadedProcessVariables = loadedVariables.get(process);
        if (loadedProcessVariables == null || !loadedProcessVariables.containsKey(name)) {
            return dao.get(process, name);
        }
        return loadedProcessVariables.get(name);
    }

    @Override
    public List<CurrentVariable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        return dao.findByNameLikeAndStringValueEqualTo(variableNamePattern, stringValue);
    }

    @Override
    public Map<String, Object> getAll(CurrentProcess process) {
        return dao.getAll(process);
    }
}
