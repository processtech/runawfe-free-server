/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.wfe.validation;

import java.util.Map;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

/**
 * Base class for field validators.
 */
public abstract class FieldValidator extends Validator {
    public static final String FIELD_NAME_PARAMETER_NAME = "fieldName";
    private String fieldName;
    private Object fieldValue;

    @Override
    public void init(ExecutionContext executionContext, VariableProvider variableProvider, ValidatorConfig config,
            ValidatorContext validatorContext, Map<String, Object> variables) {
        super.init(executionContext, variableProvider, config, validatorContext, variables);
        fieldName = config.getParams().get(FIELD_NAME_PARAMETER_NAME);
        VariableProvider newVariableProvider = new MapDelegableVariableProvider(variables, variableProvider);
        fieldValue = newVariableProvider.getValue(fieldName);
    }

    // back-compatibility only
    public void init(User user, ExecutionContext executionContext, VariableProvider variableProvider, ValidatorConfig config,
            ValidatorContext validatorContext, Map<String, Object> variables) {
        init(executionContext, variableProvider, config, validatorContext, variables);
    }

    public String getFieldName() {
        return fieldName;
    }

    protected Object getFieldValue() {
        return fieldValue;
    }

    @Override
    protected void addError(String userMessage) {
        getValidatorContext().addFieldError(fieldName, userMessage);
    }

}
