package ru.runa.wfe.presentation.filter.dialect;

public class GenericDurationDialect implements DurationDialect {

    @Override
    public String convertOperator(String fields) {
        final StringBuilder sb = new StringBuilder(255);
        sb.append("(").append(fields).append(")");
        return sb.toString();
    }

    @Override
    public String wrapParameter(final String param) {
        return ":" + param;
    }

    @Override
    public String convertValue(final int value) {
        return Integer.toString(value);
    }
}
