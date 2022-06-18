package ru.runa.wfe.office.storage.convert;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatVisitor;

/**
 * @author Alekseev Mikhail
 * @since #1394
 */
public class BaseSqlValueConverter implements VariableFormatVisitor<String, ConverterContext> {
    public static final String SQL_VALUE_NVARCHAR = "N''{0}''";
    public static final String SQL_VALUE_VARCHAR = "''{0}''";
    public static final String SQL_VALUE_NULL = "NULL";

    public static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat FORMAT_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String onDate(DateFormat dateFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_VARCHAR, FORMAT_DATE.format(converterContext.getVariableValue()));
    }

    @Override
    public String onTime(TimeFormat timeFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_VARCHAR, FORMAT_TIME.format(converterContext.getVariableValue()));
    }

    @Override
    public String onDateTime(DateTimeFormat dateTimeFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_VARCHAR, FORMAT_DATETIME.format(converterContext.getVariableValue()));
    }

    @Override
    public String onExecutor(ExecutorFormat executorFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_NVARCHAR, ((Executor) converterContext.getVariableValue()).getName());
    }

    @Override
    public String onBoolean(BooleanFormat booleanFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return ((Boolean) converterContext.getVariableValue()) ? "1" : "0";
    }

    @Override
    public String onBigDecimal(BigDecimalFormat bigDecimalFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return converterContext.getVariableValue().toString();
    }

    @Override
    public String onDouble(DoubleFormat doubleFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return converterContext.getVariableValue().toString();
    }

    @Override
    public String onLong(LongFormat longFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return converterContext.getVariableValue().toString();
    }

    @Override
    public String onFile(FileFormat fileFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_NVARCHAR, ((FileVariable) converterContext.getVariableValue()).getName());
    }

    @Override
    public String onHidden(HiddenFormat hiddenFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_NVARCHAR, converterContext.getVariableValue());
    }

    @Override
    public String onList(ListFormat listFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String onMap(MapFormat mapFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public String onProcessId(ProcessIdFormat processIdFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_VARCHAR, ((Long) converterContext.getVariableValue()).toString());
    }

    @Override
    public String onString(StringFormat stringFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_NVARCHAR, converterContext.getVariableValue());
    }

    @Override
    public String onTextString(TextFormat textFormat, ConverterContext converterContext) {
        return onString(textFormat, converterContext);
    }

    @Override
    public String onFormattedTextString(FormattedTextFormat textFormat, ConverterContext converterContext) {
        return onString(textFormat, converterContext);
    }

    @Override
    public String onUserType(UserTypeFormat userTypeFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_NVARCHAR, userTypeFormat.getUserType().getName());
    }

    @Override
    public String onOther(VariableFormat variableFormat, ConverterContext converterContext) {
        if (converterContext.getVariableValue() == null) {
            return SQL_VALUE_NULL;
        }
        return MessageFormat.format(SQL_VALUE_NVARCHAR, converterContext.getVariableValue());
    }
}
