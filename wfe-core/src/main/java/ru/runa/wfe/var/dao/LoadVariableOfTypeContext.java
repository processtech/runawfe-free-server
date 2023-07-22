package ru.runa.wfe.var.dao;

import java.util.Map;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.Variable;
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
    private final Process process;

    /**
     * Variable loader to get variables from database.
     */
    private final VariableLoader variableLoader;

    /**
     * Loading variable definition.
     */
    public final VariableDefinition variableDefinition;

    /**
     * If this map is not null then we use do not load data from DB.
     */
    private Map<String, Variable<?>> preloadedVariables;

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
    public LoadVariableOfTypeContext(ProcessDefinition processDefinition, Process process, VariableLoader variableLoader,
            Map<String, Variable<?>> preloadedVariables, VariableDefinition variableDefinition) {
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
        return new LoadVariableOfTypeContext(processDefinition, process, variableLoader, preloadedVariables, variableDefinition);
    }

    public Variable<?> getVariable() {
        if (preloadedVariables != null) {
            return preloadedVariables.get(variableDefinition.getName());
        }
        return variableLoader.get(process, variableDefinition.getName());
    }
}
