package ru.runa.wfe.var.dao;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Supports variable loading via {@link VariableDAO} and converting to {@link WfVariable}.
 */
public interface VariableLoader {

    /**
     * Get variable with given name for process.
     *
     * @param process
     *            Process, which variable must be loaded.
     * @param name
     *            Variable name.
     * @return Variable or null, if no variable found.
     */
    Variable<?> get(Process process, String name);

    /**
     * Find all variables in all processes, which name is like namePattern and value is equals to stringValue.
     *
     * @param variableNamePattern
     *            Variable name pattern, which may be exact match or contains wildcards for like search.
     * @param stringValue
     *            Exact string variable value.
     * @return all variable, found by criteria.
     */
    List<Variable<?>> findByNameLikeAndStringValueEqualTo(String variableNamePattern, String stringValue);

    /**
     * Load all variables for given process.
     *
     * @param process
     *            Process, which variables must be loaded.
     * @return all process variables.
     */
    Map<String, Object> getAll(Process process);

    /**
     * Load variable.
     *
     * @param processDefinition
     *            Process definition.
     * @param process
     *            Process instance for loading variable from.
     * @param variableName
     *            Loading variable name.
     * @return Loaded variable or null if no such variable defined.
     */
    WfVariable getVariable(ProcessDefinition processDefinition, Process process, String variableName);

    /**
     * Load variable value.
     *
     * @param processDefinition
     *            Process definition.
     * @param process
     *            Process instance for loading variable from.
     * @param variableName
     *            Loading variable name.
     * @return Loaded variable value or null.
     */
    Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition);

}
