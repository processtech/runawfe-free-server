package ru.runa.wfe.office.storage.convert;

import java.text.MessageFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;

/**
 * @author Alekseev Mikhail
 * @since #1394
 */
public class OracleSqlValueConverter extends BaseSqlValueConverter {
    @Override
    public String onDate(DateFormat dateFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format("DATE" + SQL_VALUE_VARCHAR, FORMAT_DATE.format(converterContext.getVariableValue()));
    }

    @Override
    public String onDateTime(DateTimeFormat dateTimeFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format("TIMESTAMP" + SQL_VALUE_VARCHAR, FORMAT_DATETIME.format(converterContext.getVariableValue()));
    }
}
