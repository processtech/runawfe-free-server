package ru.runa.wfe.validation;

import com.google.common.base.MoreObjects;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

public abstract class Validator {
    protected final Log log = LogFactory.getLog(getClass());
    private ExecutionContext executionContext;
    private VariableProvider oldVariableProvider;
    private VariableProvider variableProvider;
    private ValidatorConfig config;
    private ValidatorContext validatorContext;
    private Map<String, Object> newVariables;

    public void init(ExecutionContext executionContext, VariableProvider variableProvider, ValidatorConfig config, ValidatorContext validatorContext,
            Map<String, Object> variables) {
        this.executionContext = executionContext;
        this.oldVariableProvider = variableProvider;
        this.variableProvider = new MapDelegableVariableProvider(config.getParams(), new MapDelegableVariableProvider(variables, variableProvider));
        this.config = config;
        this.validatorContext = validatorContext;
        this.newVariables = variables;
    }

    protected ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Used by TNMS. Access only to old values (without submitted ones).
     */
    @SuppressWarnings("unused")
    protected VariableProvider getOldVariableProvider() {
        return oldVariableProvider;
    }

    protected VariableProvider getVariableProvider() {
        return variableProvider;
    }

    public ValidatorConfig getConfig() {
        return config;
    }

    protected ValidatorContext getValidatorContext() {
        return validatorContext;
    }

    /**
     * Used by TNMS. Access only to submitted values (with previous ones).
     */
    @SuppressWarnings("unused")
    protected Map<String, Object> getNewVariables() {
        return newVariables;
    }

    private <T> T getParameter(Class<T> clazz, String name) {
        String stringValue = config.getParams().get(name);
        if (stringValue == null) {
            return null;
        }
        Object value = ExpressionEvaluator.evaluateVariable(variableProvider, stringValue);
        return TypeConversionUtil.convertTo(clazz, value);
    }

    protected <T> T getParameter(Class<T> clazz, String name, T defaultValue) {
        T value = getParameter(clazz, name);
        return value == null ? defaultValue : value;
    }

    @SuppressWarnings("SameParameterValue")
    protected <T> T getParameterNotNull(Class<T> clazz, String name) {
        T value = getParameter(clazz, name);
        if (value == null) {
            throw new InternalApplicationException("parameter '" + name + "' is null");
        }
        return value;
    }

    public String getMessage() {
        String m = ExpressionEvaluator.substitute(config.getMessage(), variableProvider);
        return m.replaceAll("\t", " ").replaceAll("\n", " ").trim();
    }

    protected void addError(String userMessage) {
        validatorContext.addGlobalError(userMessage);
    }

    protected void addError() {
        addError(getMessage());
    }

    public abstract void validate() throws Exception;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("config", config).toString();
    }
}
