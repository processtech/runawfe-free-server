package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.VariableDAO;
import ru.runa.wfe.var.dao.VariableLoader;

/**
 * Context for {@link ConvertToSimpleVariables} operation.
 */
public class ConvertToSimpleVariablesContext {
    /**
     * Variable definition, which must be converted to variables, stored to database.
     */
    public final VariableDefinition variableDefinition;
    /**
     * Variable value.
     */
    public final Object value;
    /**
     * Loader for loading variables.
     */
    public final VariableLoader variableLoader;
    /**
     * Process definition for process, which variables will be saved.
     */
    public final ProcessDefinition processDefinition;
    /**
     * Process instance for saving variables to.
     */
    public final Process process;
    /**
     * DAO instance for work with variables.
     */
    public final VariableDAO variableDAO;

    /**
     * Creates context for {@link ConvertToSimpleVariables} operation.
     *
     * @param variableDefinition
     *            Variable definition, which must be converted to variables, stored to database.
     * @param value
     *            Variable value.
     * @param processDefinition
     *            Process definition for process, which variables will be saved.
     * @param process
     *            Process instance for saving variables to.
     * @param variableLoader
     *            Loader for loading variables.
     * @param variableDAO
     *            DAO instance for work with variables.
     */
    public ConvertToSimpleVariablesContext(VariableDefinition variableDefinition, Object value, ProcessDefinition processDefinition, Process process,
            VariableLoader variableLoader, VariableDAO variableDAO) {
        super();
        this.variableDefinition = variableDefinition;
        this.value = value;
        this.processDefinition = processDefinition;
        this.process = process;
        this.variableLoader = variableLoader;
        this.variableDAO = variableDAO;
    }

    /**
     * Creates context copy for converting value for specified variable.
     *
     * @param variableDefinition
     *            Variable definition, which must be converted to variables, stored to database.
     * @param variableValue
     *            Variable value.
     * @return Returns context copy for converting value for specified variable.
     */
    public ConvertToSimpleVariablesContext createFor(VariableDefinition variableDefinition, Object variableValue) {
        return new ConvertToSimpleVariablesContext(variableDefinition, variableValue, processDefinition, process, variableLoader, variableDAO);
    }
}
