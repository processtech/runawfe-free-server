package ru.runa.wfe.office.storage;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class PostgreSqlStoreService extends JdbcStoreService {

    @SuppressWarnings({ "serial" })
    private static final Map<Class<? extends VariableFormat>, String> typeMap = new HashMap<Class<? extends VariableFormat>, String>() {
        {
            put(StringFormat.class, "varchar(4000)");
            put(TextFormat.class, "varchar(4000)");
            put(FormattedTextFormat.class, "varchar(4000)");
            put(LongFormat.class, "bigint");
            put(DoubleFormat.class, "real");
            put(BooleanFormat.class, "varchar(5)");
            put(BigDecimalFormat.class, "decimal");
            put(DateTimeFormat.class, "timestamp");
            put(DateFormat.class, "date");
            put(TimeFormat.class, "time");

        }
    };

    public PostgreSqlStoreService(IVariableProvider variableProvider) {
        super(variableProvider);
    }

    @Override
    protected String tableExistsSql() {
        return "select null from information_schema.tables Where table_schema = ''public'' and table_name = ''{0}''";
    }

    @Override
    protected Map<Class<? extends VariableFormat>, String> typeMap() {
        return typeMap;
    }

    @Override
    protected String driverClassName() {
        return "org.postgresql.Driver";
    }

}
