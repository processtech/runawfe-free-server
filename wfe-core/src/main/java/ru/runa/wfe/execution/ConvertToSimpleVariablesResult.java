package ru.runa.wfe.execution;

import ru.runa.wfe.var.VariableDefinition;

/**
 * Result for {@link ConvertToSimpleVariables} operation. Must contains only simple variables, which may not require additional transformations before
 * saving it to database.
 */
public class ConvertToSimpleVariablesResult {

    /**
     * Saving variable definition.
     */
    public final VariableDefinition variableDefinition;

    /**
     * Saving variable value.
     */
    public final Object value;

    /**
     * Creates result for {@link ConvertToSimpleVariables} operation.
     *
     * @param context
     *            {@link ConvertToSimpleVariables} operation context for creating result for simple variable.
     */
    public ConvertToSimpleVariablesResult(ConvertToSimpleVariablesContext context) {
        variableDefinition = context.getVariableDefinition();
        value = context.getValue();
    }

    /**
     * Creates result for {@link ConvertToSimpleVariables} operation.
     * 
     * @param variableDefinition
     *            Saving variable definition.
     * @param value
     *            Saving variable value.
     */
    public ConvertToSimpleVariablesResult(VariableDefinition variableDefinition, Object value) {
        this.variableDefinition = variableDefinition;
        this.value = value;
    }
}
