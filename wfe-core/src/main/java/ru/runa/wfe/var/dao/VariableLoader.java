package ru.runa.wfe.var.dao;

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

public abstract class VariableLoader {
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Get variable with given name for process.
     *
     * @param process
     *            Process, which variable must be loaded.
     * @param name
     *            Variable name.
     * @return Variable or null, if no variable found.
     */
    public abstract Variable get(Process process, String name);

    /**
     * Load all variables for given process.
     *
     * @param process
     *            Process, which variables must be loaded.
     * @return all process variables.
     */
    public abstract Map<String, Object> getAll(Process process);

    /**
     * Load variable.
     *
     * @param parsedProcessDefinition
     *            Process definition.
     * @param process
     *            Process instance for loading variable from.
     * @param variableName
     *            Loading variable name.
     * @return Loaded variable or null if no such variable defined.
     */
    public WfVariable getVariable(ParsedProcessDefinition parsedProcessDefinition, Process process, String variableName) {
        VariableDefinition variableDefinition = parsedProcessDefinition.getVariable(variableName, false);
        if (variableDefinition != null) {
            Object variableValue = getVariableValue(parsedProcessDefinition, process, variableDefinition);
            if (variableValue == null && SystemProperties.isV4ListVariableCompatibilityMode()
                    && variableName.endsWith(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                int startQualifierIndex = variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                int endQualifierIndex = variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_END);
                String listVariableName = variableName.substring(0, startQualifierIndex);
                int listIndex = Integer.parseInt(variableName.substring(startQualifierIndex + 1, endQualifierIndex));
                VariableDefinition listVariableDefinition = parsedProcessDefinition.getVariable(listVariableName, false);
                List<Object> list = (List<Object>) getVariableValue(parsedProcessDefinition, process, listVariableDefinition);
                if (list != null) {
                    if (list.size() > listIndex) {
                        variableValue = list.get(listIndex);
                    } else {
                        log.warn("Strange list when requesting " + process + ":" + variableName + ": " + list);
                    }
                }
            }
            return new WfVariable(variableDefinition, variableValue);
        }
        if (SystemProperties.isV3CompatibilityMode()) {
            Variable variable = get(process, variableName);
            return new WfVariable(variableName, variable != null ? variable.getValue() : null);
        }
        log.debug("No variable defined by name '" + variableName + "' in " + process + ", returning null");
        return null;
    }

    /**
     * Load variable value.
     *
     * @param parsedProcessDefinition
     *            Process definition.
     * @param process
     *            Process instance for loading variable from.
     * @param variableDefinition
     *            Loading variable name.
     * @return Loaded variable value or null.
     */
    public Object getVariableValue(ParsedProcessDefinition parsedProcessDefinition, Process process, VariableDefinition variableDefinition) {
        LoadVariableOfTypeContext context = new LoadVariableOfTypeContext(parsedProcessDefinition, process, this, variableDefinition);
        switch (variableDefinition.getStoreType()) {
            case BLOB:
                return new LoadVariableOfType().onOther(variableDefinition.getFormatNotNull(), context);
            default:
                return variableDefinition.getFormatNotNull().processBy(new LoadVariableOfType(), context);
        }
    }
}
