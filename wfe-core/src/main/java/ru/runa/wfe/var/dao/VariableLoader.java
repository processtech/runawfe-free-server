package ru.runa.wfe.var.dao;

import java.util.Map;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Supports variable loading via {@link VariableDao} and converting to {@link WfVariable}.
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
    BaseVariable get(Process process, String name);

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
     * @return Loaded variable value or null.
     */
    Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition);

}
