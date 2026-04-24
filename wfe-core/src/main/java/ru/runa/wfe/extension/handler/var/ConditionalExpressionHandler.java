package ru.runa.wfe.extension.handler.var;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ConditionalHandler;
import ru.runa.wfe.lang.ConditionalEventModel;

public class ConditionalExpressionHandler implements ConditionalHandler {

    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        String expression = ConditionalEventModel.fromXml(configuration).getExpression();

        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Expression is not set for Conditional handler");
        }

        this.configuration = expression;
    }

    @Override
    public boolean evaluate(ExecutionContext context) throws Exception {

        Object result = new GroovyScriptExecutor()
                .evaluateScript(context.getVariableProvider(), configuration);

        return Boolean.parseBoolean(String.valueOf(result));
    }
}
