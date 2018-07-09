package ru.runa.wfe.presentation.filter.dialect;

import org.apache.commons.lang.time.DurationFormatUtils;

public class PostgreSqlDurationDialect extends GenericDurationDialect {

    @Override
    public String convertOperator(String fields) {
        final StringBuilder sb = new StringBuilder(255);
        sb.append("(").append(fields.replace("current_date", "current_timestamp")).append(")");
        return sb.toString();
    }
    
    @Override
    public String wrapParameter(String param) {
        final StringBuilder sb = new StringBuilder(255);
        sb.append("CAST( :").append(param).append(" as interval)");
        return sb.toString();
    }

    @Override
    public String convertValue(int value) {
        return DurationFormatUtils.formatDuration(value * 60 * 1000, "d HH:mm:ss");
    }
}
