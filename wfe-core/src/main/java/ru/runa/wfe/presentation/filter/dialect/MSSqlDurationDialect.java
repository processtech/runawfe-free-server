package ru.runa.wfe.presentation.filter.dialect;

public class MSSqlDurationDialect extends DateDiffDurationDialect {

    @Override
    public String convertOperator(String fields) {
        return super.convertOperator(fields.replace("current_date", "getdate()"));
    }
}
