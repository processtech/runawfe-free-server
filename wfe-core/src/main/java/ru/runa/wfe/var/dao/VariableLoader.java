package ru.runa.wfe.var.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Modes:
 * * WITHOUT_DAO: only preloadedOutsideVariables used
 * * WITH_DAO_BATCH_PRELOADED: preloadedBatchVariables are prefilled for complex variables (user types, lists, maps), dao called only for simple variables
 * * WITH_DAO_DIRECT: dao called for each variable if preloadedOutsideVariables does not contain result
 */
public class VariableLoader {
    private static final Log log = LogFactory.getLog(VariableLoader.class);

    private final VariableDao dao;
    private final Map<Process, Map<String, Variable<?>>> preloadedOutsideVariables = new HashMap<>();

    public VariableLoader(VariableDao dao, Map<Process, Map<String, Variable<?>>> preloadedVariables) {
        this.dao = dao;
        if (preloadedVariables != null) {
            this.preloadedOutsideVariables.putAll(preloadedVariables);
        }
    }

    public VariableLoader(Map<Process, Map<String, Variable<?>>> loadedVariables) {
        this(null, loadedVariables);
    }

    public Variable<?> get(Process process, String name) {
        Map<String, Variable<?>> loadedProcessVariables = preloadedOutsideVariables.get(process);
        if (loadedProcessVariables != null && loadedProcessVariables.containsKey(name)) {
            return loadedProcessVariables.get(name);
        }
        if (dao != null) {
            return dao.get(process, name);
        }
        return null;
    }

    public WfVariable getVariable(ProcessDefinition processDefinition, Process process, String variableName) {
        VariableDefinition variableDefinition = processDefinition.getVariable(variableName, false);
        if (variableDefinition != null) {
            Object variableValue = getVariableValue(processDefinition, process, variableDefinition);
            if (variableValue == null && SystemProperties.isV4ListVariableCompatibilityMode()
                    && variableName.endsWith(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                int startQualifierIndex = variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                int endQualifierIndex = variableName.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_END);
                String listVariableName = variableName.substring(0, startQualifierIndex);
                int listIndex = Integer.parseInt(variableName.substring(startQualifierIndex + 1, endQualifierIndex));
                VariableDefinition listVariableDefinition = processDefinition.getVariable(listVariableName, false);
                List<Object> list = (List<Object>) getVariableValue(processDefinition, process, listVariableDefinition);
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
            Variable<?> variable = get(process, variableName);
            return new WfVariable(variableName, variable != null ? variable.getValue() : null);
        }
        log.debug("No variable defined by name '" + variableName + "' in " + process + ", returning null");
        return null;
    }

    public Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition) {
        VariableFormat format = variableDefinition.getFormatNotNull();
        boolean preloadBatchVariables = dao != null && format.canBePersistedAsComplexVariable();
        Map<String, Variable<?>> preloadedBatchVariables = new HashMap<>();
        if (preloadBatchVariables) {
            final List<Variable<?>> variables = dao.getVariablesByNameStartsWith(process, variableDefinition.getName());
            for (Variable<?> variable : variables) {
                preloadedBatchVariables.put(variable.getName(), variable);
            }
        }
        LoadVariableOfTypeContext context = new LoadVariableOfTypeContext(processDefinition, process, this, preloadedBatchVariables,
                variableDefinition);
        switch (variableDefinition.getStoreType()) {
        case BLOB:
            return new LoadVariableOfType().onOther(format, context);
        default:
            return format.processBy(new LoadVariableOfType(), context);
        }
    }
}
