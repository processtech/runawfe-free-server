package ru.runa.wfe.execution;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.VariableDAO;

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
     * Loading variables execution context.
     */
    public final ExecutionContext executionContext;
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
     *            Loading variables execution context.
     * @param variableDAO
     *            DAO instance for work with variables.
     */
    public ConvertToSimpleVariablesContext(VariableDefinition variableDefinition, Object value, ProcessDefinition processDefinition, Process process,
            ExecutionContext executionContext, VariableDAO variableDAO) {
        super();
        this.variableDefinition = variableDefinition;
        this.value = value;
        this.processDefinition = processDefinition;
        this.process = process;
        this.executionContext = executionContext;
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
        return new ConvertToSimpleVariablesContext(variableDefinition, variableValue, processDefinition, process, executionContext, variableDAO);
    }
}
