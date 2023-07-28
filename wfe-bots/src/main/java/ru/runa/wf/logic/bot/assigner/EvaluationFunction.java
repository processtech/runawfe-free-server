package ru.runa.wf.logic.bot.assigner;

import ru.runa.wfe.var.VariableProvider;

public interface EvaluationFunction {

    boolean evaluate(VariableProvider variableProvider);
}
