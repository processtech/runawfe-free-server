package ru.runa.wfe.execution;

import org.apache.commons.logging.Log;

import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Context for {@link ConvertToSimpleVariables} operation, which used to unroll variables without any references to current process state.
 */
public class ConvertToSimpleVariablesUnrollContext implements ConvertToSimpleVariablesContext {
    /**
     * Variable definition, which must be converted to variables, stored to database.
     */
    private final VariableDefinition variableDefinition;
    /**
     * Variable value.
     */
    private final Object value;

    /**
     * Creates context for {@link ConvertToSimpleVariables} operation.
     *
     * @param variableDefinition
     *            Variable definition, which must be converted to variables, stored to database.
     * @param value
     *            Variable value.
     */
    public ConvertToSimpleVariablesUnrollContext(VariableDefinition variableDefinition, Object value) {
        super();
        this.variableDefinition = variableDefinition;
        this.value = value;
    }

    @Override
    public ConvertToSimpleVariablesContext createFor(VariableDefinition variableDefinition, Object variableValue) {
        return new ConvertToSimpleVariablesUnrollContext(variableDefinition, variableValue);
    }

    @Override
    public WfVariable loadCurrentVariableStat(String variableName) {
        return null;
    }

    @Override
    public void remove(Log log) {
    }

    @Override
    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
