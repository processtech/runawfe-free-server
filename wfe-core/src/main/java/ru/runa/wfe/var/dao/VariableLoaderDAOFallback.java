package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Supports variable loading via {@link VariableDAO} and converting to {@link WfVariable}. Variables may be preloaded and passed to this component in
 * case of mass variables loading.
 *
 * @author AL
 */
@SuppressWarnings({ "unchecked" })
public class VariableLoaderDAOFallback extends BaseVariableLoaderImpl {

    /**
     * Logging support.
     */
    private static final Log log = LogFactory.getLog(VariableLoaderDAOFallback.class);

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
     */
    public VariableLoaderDAOFallback(VariableDAO dao, Map<Process, Map<String, Variable<?>>> loadedVariables, boolean disableDaoFallback) {
        this.dao = dao;
        this.loadedVariables = loadedVariables == null ? new HashMap<Process, Map<String, Variable<?>>>() : loadedVariables;
        this.disableDaoFallback = disableDaoFallback;
    }

    @Override
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

    @Override
    public List<Variable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue) {
        return dao.findByNameLikeAndStringValueEqualTo(variableNamePattern, stringValue);
    }

    @Override
    public Map<String, Object> getAll(Process process) {
        return dao.getAll(process);
    }
}
