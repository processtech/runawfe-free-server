package ru.runa.wfe.validation.impl;

import com.google.common.collect.Maps;
import java.util.Map;
import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.ScriptExecutor;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.validation.Validator;
import ru.runa.wfe.validation.ValidatorException;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

public class GroovyExpressionValidator extends Validator {

    protected ScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    public void validate() {
        try {
            ScriptExecutor scriptExecutor = getScriptExecutor();
            String expression = getParameterNotNull(String.class, "expression");
            Map<String, Object> variables = Maps.newHashMap();
            variables.put("validator", this);
            VariableProvider validatorVariableProvider = new MapDelegableVariableProvider(variables, getVariableProvider());
            Object result = scriptExecutor.evaluateScript(validatorVariableProvider, expression);
            if (!Boolean.TRUE.equals(result)) {
                addError();
            }
        } catch (ValidatorException e) {
            log.warn(e);
            addError(e.getMessage());
        } catch (Exception e) {
            log.error("Groovy", e);
            if (SystemProperties.showErrorsInGroovyExpressionValidator()) {
                addError(e.toString());
            } else {
                addError();
            }
        }
    }

}
