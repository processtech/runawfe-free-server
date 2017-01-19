package ru.runa.wfe.presentation.filter.dialect;

public class Oracle9DurationDialect extends GenericDurationDialect {

    @Override
    public String convertOperator(String fields) {
        final StringBuilder sb = new StringBuilder(255);
        sb.append("(").append(fields.replace("current_date", "sysdate")).append(") * 24 * 60");
        return sb.toString();
    }
}
