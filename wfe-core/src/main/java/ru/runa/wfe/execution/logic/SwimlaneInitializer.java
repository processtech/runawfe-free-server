package ru.runa.wfe.execution.logic;

import java.util.List;

import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableProvider;

public abstract class SwimlaneInitializer {
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";

    public abstract void parse(String swimlaneConfiguration);

    public abstract List<? extends Executor> evaluate(VariableProvider variableProvider);
}
