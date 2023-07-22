package ru.runa.wfe.extension.handler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.collect.Lists;

/**
 * Parameters holder for handler.
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class HandlerData {
    private static final Log log = LogFactory.getLog(HandlerData.class);
    private final ParamsDef paramsDef;
    private final Map<String, Object> outputVariables = new HashMap<String, Object>();
    private final VariableProvider variableProvider;
    // case of action handler
    private final ExecutionContext executionContext;
    // case of task handler
    private final User user;
    private final WfTask task;

    public HandlerData(ParamsDef paramsDef, ExecutionContext executionContext) {
        this.paramsDef = paramsDef;
        this.variableProvider = executionContext.getVariableProvider();
        this.executionContext = executionContext;
        this.user = null;
        this.task = null;
    }

    public HandlerData(ParamsDef paramsDef, User user, VariableProvider variableProvider, WfTask task) {
        this.paramsDef = paramsDef;
        this.variableProvider = variableProvider;
        this.executionContext = null;
        this.user = user;
        this.task = task;
    }

    public Long getProcessId() {
        if (executionContext != null) {
            return executionContext.getProcess().getId();
        }
        return task.getProcessId();
    }

    public String getTaskName() {
        if (executionContext != null) {
            // used for logging purposes
            return getClass().getSimpleName();
        }
        return task.getName();
    }

    public String getDefinitionName() {
        if (executionContext != null) {
            return executionContext.getProcessDefinition().getName();
        }
        return task.getDefinitionName();
    }

    public Map<String, Object> getOutputVariables() {
        return outputVariables;
    }

    /**
     * @return not-null context for action handler only
     */
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * @return not-null user for task handler only
     */
    public User getUser() {
        return user;
    }

    /**
     * @return not-null task for task handler only
     */
    public WfTask getTask() {
        return task;
    }

    /**
     * @return subprocess ids hierarchy from root process to current process
     */
    public List<Long> getProcessIdsHierarchy() {
        String hierarchyIds;
        if (executionContext != null) {
            hierarchyIds = executionContext.getProcess().getHierarchyIds();
        } else {
            hierarchyIds = task.getProcessHierarchyIds();
        }
        return ProcessHierarchyUtils.getProcessIds(hierarchyIds);
    }

    /**
     * @return subprocess ids hierarchy from current process to root process
     */
    public List<Long> getProcessIdsHierarchyInversed() {
        List<Long> reversed = Lists.newArrayList(getProcessIdsHierarchy());
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * @return input parameter value or throws exception (in case of parameter
     *         is not defined or has <code>null</code> value)
     */
    public <T> T getInputParamValueNotNull(String name) {
        return (T) paramsDef.getInputParamValueNotNull(name, variableProvider);
    }

    /**
     * @return input parameter value or default value (in case of parameter is
     *         not defined or has <code>null</code> value)
     */
    public <T> T getInputParamValue(String name, T defaultValue) {
        Object result = paramsDef.getInputParamValue(name, variableProvider);
        if (result == null) {
            return defaultValue;
        }
        return (T) result;
    }

    /**
     * @return input parameter value or <code>null</code>
     */
    public <T> T getInputParamValue(String name) {
        return (T) getInputParamValue(name, null);
    }

    /**
     * @return input parameter value casted to specified class or throws
     *         exception (in case of parameter is not defined or has
     *         <code>null</code> value)
     */
    public <T> T getInputParamValueNotNull(Class<T> clazz, String name) {
        Object object = getInputParamValueNotNull(name);
        return TypeConversionUtil.convertTo(clazz, object);
    }

    /**
     * @return input parameter value casted to specified class or default value
     *         (in case of parameter is not defined or has <code>null</code>
     *         value)
     */
    public <T> T getInputParamValue(Class<T> clazz, String name, T defaultValue) {
        Object object = getInputParamValue(name, defaultValue);
        return TypeConversionUtil.convertTo(clazz, object);
    }

    /**
     * @return input parameter value casted to specified class or
     *         <code>null</code>
     */
    public <T> T getInputParamValue(Class<T> clazz, String name) {
        return getInputParamValue(clazz, name, null);
    }

    public VariableProvider getVariableProvider() {
        return variableProvider;
    }

    public ParamsDef getParamsDef() {
        return paramsDef;
    }

    public Map<String, ParamDef> getInputParams() {
        return paramsDef.getInputParams();
    }

    public Map<String, ParamDef> getOutputParams() {
        return paramsDef.getOutputParams();
    }

    public ParamDef getOutputParamNotNull(String name) {
        return paramsDef.getOutputParamNotNull(name);
    }

    public void setOutputOptionalParam(String name, Object value) {
        ParamDef paramDef = paramsDef.getOutputParam(name);
        if (paramDef == null) {
            log.debug("Optional output parameter " + name + " does not defined in configuration.");
            return;
        }
        if (paramDef.getVariableName() == null) {
            log.warn("Variable not set for output parameter " + paramDef + " in configuration.");
            return;
        }
        setOutputVariable(paramDef.getVariableName(), value);
    }

    public void setOutputParam(String name, Object value) {
        ParamDef paramDef = paramsDef.getOutputParamNotNull(name);
        if (value == null) {
            throw new InternalApplicationException("Trying to set output parameter " + paramDef + " to null.");
        }
        setOutputVariable(paramDef.getVariableName(), value);
    }

    public void setOutputVariable(String variableName, Object value) {
        if (variableName == null) {
            throw new InternalApplicationException("Trying to set output variable with null name.");
        }
        outputVariables.put(variableName, value);
    }
}
