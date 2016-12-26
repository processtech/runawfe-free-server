package ru.runa.wfe.presentation.filter.dialect;

public class DateDiffDurationDialect extends GenericDurationDialect {

    @Override
    public String convertOperator(final String fields) {
        final StringBuilder sb = new StringBuilder(255);
        sb.append("-DATEDIFF(").append(getSecondTag()).append(", ").append(fields.replace('-', ',')).append(")");
        return sb.toString();
    }

    protected String getSecondTag() {
        return "'SECOND'";
    }

    @Override
    public String convertValue(final int value) {
        return Integer.toString(value * 60);
    }
}
