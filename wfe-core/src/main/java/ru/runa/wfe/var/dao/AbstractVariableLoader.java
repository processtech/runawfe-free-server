package ru.runa.wfe.var.dao;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Base implementation for {@link VariableLoader}. Contains common methods implementation.
 */
public abstract class AbstractVariableLoader implements VariableLoader {
    /**
     * Logging support.
     */
    protected final Log log = LogFactory.getLog(getClass());

    @Override
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
            BaseVariable variable = get(process, variableName);
            return new WfVariable(variableName, variable != null ? variable.getValue() : null);
        }
        log.debug("No variable defined by name '" + variableName + "' in " + process + ", returning null");
        return null;
    }

    @Override
    public Object getVariableValue(ProcessDefinition processDefinition, Process process, VariableDefinition variableDefinition) {
        LoadVariableOfTypeContext context = new LoadVariableOfTypeContext(processDefinition, process, this, variableDefinition);
        switch (variableDefinition.getStoreType()) {
        case BLOB:
            return new LoadVariableOfType().onOther(variableDefinition.getFormatNotNull(), context);
        default:
            return variableDefinition.getFormatNotNull().processBy(new LoadVariableOfType(), context);
        }
    }
}
