package ru.runa.wfe.var.dao;

import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Operation context for {@link LoadVariableOfType}.
 */
public class LoadVariableOfTypeContext {
    /**
     * Process definition for loading variable process.
     */
    public final ProcessDefinition processDefinition;

    /**
     * Process instance loading variable from.
     */
    public final CurrentProcess process;

    /**
     * Variable loader to get variables from database.
     */
    public final VariableLoader variableLoader;

    /**
     * Loading variable definition.
     */
    public final VariableDefinition variableDefinition;

    /**
     * Creates operation context for {@link LoadVariableOfType}.
     * 
     * @param processDefinition
     *            Process definition for loading variable process.
     * @param process
     *            Process instance loading variable from.
     * @param variableLoader
     *            Variable loader to get variables from database.
     * @param variableDefinition
     *            Loading variable definition.
     */
    public LoadVariableOfTypeContext(ProcessDefinition processDefinition, CurrentProcess process, VariableLoader variableLoader,
            VariableDefinition variableDefinition) {
        this.processDefinition = processDefinition;
        this.process = process;
        this.variableLoader = variableLoader;
        this.variableDefinition = variableDefinition;
    }

    /**
     * Creates context copy for loading specified variable.
     * 
     * @param variableDefinition
     *            Variable definition for loading variable variable.
     * @return Returns context copy for loading variable.
     */
    public LoadVariableOfTypeContext createFor(VariableDefinition variableDefinition) {
        return new LoadVariableOfTypeContext(processDefinition, process, variableLoader, variableDefinition);

    }
}
