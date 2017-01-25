package ru.runa.wfe.execution;

import org.apache.commons.logging.Log;

import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.VariableDAO;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Context for {@link ConvertToSimpleVariables} operation, which used on saving variables to process.
 */
public class ConvertToSimpleVariablesOnSaveContext implements ConvertToSimpleVariablesContext {
    /**
     * Variable definition, which must be converted to variables, stored to database.
     */
    private final VariableDefinition variableDefinition;
    /**
     * Variable value.
     */
    private final Object value;
    /**
     * Loading variables context.
     */
    private final BaseProcessVariableLoader baseProcessVariableLoader;
    /**
     * Process instance for saving variables to.
     */
    private final Process process;
    /**
     * DAO instance for work with variables.
     */
    private final VariableDAO variableDAO;

    /**
     * Creates context for {@link ConvertToSimpleVariables} operation.
     *
     * @param variableDefinition
     *            Variable definition, which must be converted to variables, stored to database.
     * @param value
     *            Variable value.
     * @param process
     *            Process instance for saving variables to.
     * @param baseProcessVariableLoader
     *            Loading variables context.
     * @param variableDAO
     *            DAO instance for work with variables.
     */
    public ConvertToSimpleVariablesOnSaveContext(VariableDefinition variableDefinition, Object value, Process process,
            BaseProcessVariableLoader baseProcessVariableLoader, VariableDAO variableDAO) {
        super();
        this.variableDefinition = variableDefinition;
        this.value = value;
        this.process = process;
        this.baseProcessVariableLoader = baseProcessVariableLoader;
        this.variableDAO = variableDAO;
    }

    @Override
    public ConvertToSimpleVariablesContext createFor(VariableDefinition variableDefinition, Object variableValue) {
        return new ConvertToSimpleVariablesOnSaveContext(variableDefinition, variableValue, process, baseProcessVariableLoader, variableDAO);
    }

    @Override
    public WfVariable loadCurrentVariableStat(String variableName) {
        return baseProcessVariableLoader.get(variableName);
    }

    @Override
    public void remove(Log log) {
        Variable<?> variable = variableDAO.get(process, getVariableDefinition().getName());
        if (variable != null) {
            log.debug("Removing old-style variable '" + getVariableDefinition().getName() + "'");
            variableDAO.delete(variable);
        }
    }

    @Override
    public VariableDefinition getVariableDefinition() {
        return variableDefinition;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean isVirtualVariablesRequired() {
        return false;
    }
}
