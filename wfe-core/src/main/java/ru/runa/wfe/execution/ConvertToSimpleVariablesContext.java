package ru.runa.wfe.execution;

import org.apache.commons.logging.Log;

import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public interface ConvertToSimpleVariablesContext {

    /**
     * Creates context copy for converting value for specified variable.
     *
     * @param variableDefinition
     *            Variable definition, which must be converted to variables, stored to database.
     * @param variableValue
     *            Variable value.
     * @return Returns context copy for converting value for specified variable.
     */
    ConvertToSimpleVariablesContext createFor(VariableDefinition variableDefinition, Object variableValue);

    /**
     * Loading current saved variable state.
     *
     * @param variableName
     *            Variable name for loading current state.
     * @return Returns current variable state.
     */
    WfVariable loadCurrentVariableStat(String variableName);

    /**
     * Removes variable, if it's stored in database. Must be called for obsolete variables store configuration.
     *
     * @param log
     *            Logging support.
     */
    void remove(Log log);

    /**
     * Variable definition, which must be converted to variables, stored to database.
     *
     * @return Returns variable definition, which must be converted to variables, stored to database.
     */
    VariableDefinition getVariableDefinition();

    /**
     * Variable value.
     *
     * @return Returns variable value.
     */
    Object getValue();
}