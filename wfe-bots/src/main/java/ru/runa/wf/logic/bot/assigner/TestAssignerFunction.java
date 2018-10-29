package ru.runa.wf.logic.bot.assigner;

import ru.runa.wfe.var.VariableProvider;

public class TestAssignerFunction implements EvaluationFunction {

    @Override
    public boolean evaluate(VariableProvider variableProvider) {
        return "test".equals(variableProvider.getValue("test"));
    }
}
