package ru.runa.wfe.office.storage.binding;

public enum LogicalOperator {
    AND("and"),
    OR("or"),
    OPEN_PARENTHESIS("("),
    CLOSE_PARENTHESIS(")");

    private final String command;

    LogicalOperator(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    public static LogicalOperator fromString(String s) {
        for (LogicalOperator o : LogicalOperator.values()) {
            if (o.command.equalsIgnoreCase(s)) {
                return o;
            }
        }
        return null;
    }
}
