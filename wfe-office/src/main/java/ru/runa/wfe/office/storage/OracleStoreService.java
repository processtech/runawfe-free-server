package ru.runa.wfe.office.storage;

import com.google.common.base.Charsets;
import java.util.HashMap;
import java.util.Map;
import ru.runa.wfe.office.storage.convert.ConverterContext;
import ru.runa.wfe.office.storage.convert.OracleSqlValueConverter;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatVisitor;

public class OracleStoreService extends JdbcStoreService {

    private static final int IDENTIFIER_MAX_LENGTH = 30; // bytes for Oracle <= 12.1, 128 bytes - for Oracle >= 12.2

    @SuppressWarnings({ "serial" })
    private static final Map<Class<? extends VariableFormat>, String> typeMap = new HashMap<Class<? extends VariableFormat>, String>() {
        {
            put(StringFormat.class, "nvarchar2(2000)");
            put(TextFormat.class, "nvarchar2(2000)");
            put(FormattedTextFormat.class, "nvarchar2(2000)");
            put(LongFormat.class, "number(19)");
            put(DoubleFormat.class, "number(19, 4)");
            put(BooleanFormat.class, "number(1, 0)");
            put(BigDecimalFormat.class, "number(38)");
            put(DateTimeFormat.class, "timestamp");
            put(DateFormat.class, "date");
            put(TimeFormat.class, "varchar2(5)");
            put(ExecutorFormat.class, "nvarchar2(2000)");
            put(ActorFormat.class, "nvarchar2(2000)");
            put(GroupFormat.class, "nvarchar2(2000)");
            put(ProcessIdFormat.class, "number(19)");
            put(FileFormat.class, "nvarchar2(2000)");
        }
    };

    public OracleStoreService(VariableProvider variableProvider) {
        super(variableProvider);
        System.getProperties().setProperty("oracle.jdbc.J2EE13Compliant", "true");
    }

    @Override
    protected String tableExistsSql() {
        return "select null from sys.tab where tname = N''{0}''";
    }

    @Override
    protected Map<Class<? extends VariableFormat>, String> typeMap() {
        return typeMap;
    }

    @Override
    protected String adjustIdentifier(String identifier) {
        byte[] bytes = identifier.getBytes(Charsets.UTF_8);
        if (identifier.length() == bytes.length) {
            return identifier.length() > IDENTIFIER_MAX_LENGTH ? identifier.substring(0, IDENTIFIER_MAX_LENGTH) : identifier;
        } else {
            if (bytes.length > IDENTIFIER_MAX_LENGTH) {
                do {
                    identifier = identifier.substring(0, identifier.length() - 1);
                    bytes = identifier.getBytes(Charsets.UTF_8);
                } while (bytes.length > IDENTIFIER_MAX_LENGTH);
                return new String(bytes, Charsets.UTF_8);
            } else {
                return identifier;
            }
        }
    }

    @Override
    protected VariableFormatVisitor<String, ConverterContext> sqlValueConverter() {
        return new OracleSqlValueConverter();
    }

}
