package ru.runa.wfe.validation.impl;

import java.util.Map;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.IScriptExecutor;
import ru.runa.wfe.validation.Validator;
import ru.runa.wfe.validation.ValidatorException;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

import com.google.common.collect.Maps;

public class GroovyExpressionValidator extends Validator {

    protected IScriptExecutor getScriptExecutor() {
        return new GroovyScriptExecutor();
    }

    @Override
    public void validate() {
        try {
            IScriptExecutor scriptExecutor = getScriptExecutor();
            String expression = getParameterNotNull(String.class, "expression");
            Map<String, Object> variables = Maps.newHashMap();
            variables.put("validator", this);
            IVariableProvider validatorVariableProvider = new MapDelegableVariableProvider(variables, getVariableProvider());
            Object result = scriptExecutor.evaluateScript(validatorVariableProvider, expression);
            if (!Boolean.TRUE.equals(result)) {
                addError();
            }
        } catch (ValidatorException e) {
            log.warn(e);
            addError(e.getMessage());
        } catch (Exception e) {
            log.error("Groovy", e);
            addError();
            // This is because calling side has not Groovy generated classes and
            // will unable to show exception
        }
    }

}
