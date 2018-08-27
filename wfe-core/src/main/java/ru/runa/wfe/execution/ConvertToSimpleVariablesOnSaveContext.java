package ru.runa.wfe.execution;

import org.apache.commons.logging.Log;

import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.CurrentVariableDao;
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
    private final CurrentProcess process;
    /**
     * DAO instance for work with variables.
     */
    private final CurrentVariableDao currentVariableDao;

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
     * @param currentVariableDao
     *            DAO instance for work with variables.
     */
    public ConvertToSimpleVariablesOnSaveContext(VariableDefinition variableDefinition, Object value, CurrentProcess process,
            BaseProcessVariableLoader baseProcessVariableLoader, CurrentVariableDao currentVariableDao) {
        super();
        this.variableDefinition = variableDefinition;
        this.value = value;
        this.process = process;
        this.baseProcessVariableLoader = baseProcessVariableLoader;
        this.currentVariableDao = currentVariableDao;
    }

    @Override
    public ConvertToSimpleVariablesContext createFor(VariableDefinition variableDefinition, Object variableValue) {
        return new ConvertToSimpleVariablesOnSaveContext(variableDefinition, variableValue, process, baseProcessVariableLoader, currentVariableDao);
    }

    @Override
    public WfVariable loadCurrentVariableStat(String variableName) {
        return baseProcessVariableLoader.get(variableName);
    }

    @Override
    public void remove(Log log) {
        CurrentVariable<?> variable = currentVariableDao.get(process, getVariableDefinition().getName());
        if (variable != null) {
            log.debug("Removing old-style variable '" + getVariableDefinition().getName() + "'");
            currentVariableDao.delete(variable);
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
