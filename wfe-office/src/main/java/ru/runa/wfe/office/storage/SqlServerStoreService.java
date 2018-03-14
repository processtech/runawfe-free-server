package ru.runa.wfe.office.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.datasource.JdbcDataSourceType;
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.office.excel.IExcelConstraints;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.ParamBasedVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class SqlServerStoreService implements StoreService {

    private static final Log log = LogFactory.getLog(SqlServerStoreService.class);

    private static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat FORMAT_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String SQL_TABLE_NAME_PREFIX = "SHEET";
    private static final String SQL_LIST_SEPARATOR = ", ";
    private static final String SQL_TABLE_EXIST = "select null from information_schema.tables where table_type = ''BASE TABLE'' and table_name = N''{0}'';";
    private static final String SQL_CREATE_TABLE = "create table [{0}] ({1});";
    private static final String SQL_COLUMN_DEFINITION = "[{0}] {1}";
    private static final String SQL_INSERT = "insert into [{0}] ({1}) values ({2});";
    private static final String SQL_COLUMN = "[{0}]";
    private static final String SQL_VALUE_NVARCHAR = "N''{0}''";
    private static final String SQL_VALUE_VARCHAR = "''{0}''";
    private static final String SQL_SELECT = "select {0} from [{1}] where {2};";
    private static final String SQL_UPDATE = "update [{0}] set {1} where {2}; ";
    private static final String SQL_COLUMN_SET = "[{0}] = {1}";
    private static final String SQL_DELETE = "delete from [{0}] where {1};";
    private static final String SQL_TRUE_SEARCH_CONDITION = "1 = 1";
    private static final String SQL_AND = " and ";

    @SuppressWarnings({ "serial" })
    private static final Map<Class<? extends VariableFormat>, String> typeMap = new HashMap<Class<? extends VariableFormat>, String>() {
        {
            put(StringFormat.class, "nvarchar(4000)");
            put(TextFormat.class, "nvarchar(4000)");
            put(FormattedTextFormat.class, "nvarchar(4000)");
            put(LongFormat.class, "int");
            put(DoubleFormat.class, "real"); // The ISO synonym for real is float(24).
            put(BooleanFormat.class, "nvarchar(5)");
            put(BigDecimalFormat.class, "decimal");
            put(DateTimeFormat.class, "datetime2");
            put(DateFormat.class, "date");
            put(TimeFormat.class, "time");

        }
    };

    private IExcelConstraints constraints;
    private String fullPath;
    private IVariableProvider variableProvider;
    private JdbcDataSource ds;

    public SqlServerStoreService(IVariableProvider variableProvider) {
        this.variableProvider = variableProvider;
    }

    @Override
    public void createFileIfNotExist(String path) throws Exception {
        // Do nothing
    }

    private void createTableIfNotExist(WfVariable variable) throws Exception {
        String tableName = getTableName();
        if (!executeSql(MessageFormat.format(SQL_TABLE_EXIST, tableName))) {
            UserType userType = variable.getDefinition().getUserType();
            String columnDefinitions = "";
            for (VariableDefinition vd : userType.getAttributes()) {
                columnDefinitions += (columnDefinitions.length() > 0 ? SQL_LIST_SEPARATOR : "")
                        + MessageFormat.format(SQL_COLUMN_DEFINITION, vd.getName(), typeMap.get(vd.getFormatNotNull().getClass()));
            }
            executeSql(MessageFormat.format(SQL_CREATE_TABLE, tableName, columnDefinitions));
        }
    }

    private String getTableName() {
        OnSheetConstraints osc = (OnSheetConstraints) constraints;
        String tableName = osc.getSheetName();
        if (Strings.isNullOrEmpty(tableName)) {
            tableName = SQL_TABLE_NAME_PREFIX + osc.getSheetIndex();
        }
        return tableName;
    }

    @Override
    public ExecutionResult findByFilter(Properties properties, WfVariable variable, String condition) throws Exception {
        if (!existOutputParamByVariableName(variable)) {
            return ExecutionResult.EMPTY;
        }
        initParams(properties, variable);
        String columns = "";
        UserType ut = variable.getDefinition().getFormatComponentUserTypes()[0];
        for (VariableDefinition vd : ut.getAttributes()) {
            String variableName = vd.getName();
            columns += (columns.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN, variableName);
        }
        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(MessageFormat.format(SQL_SELECT, columns, getTableName(), condition(condition)))) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<UserTypeMap> utmList = Lists.newArrayList();
                    while (rs.next()) {
                        UserTypeMap utm = new UserTypeMap(ut);
                        for (VariableDefinition vd : ut.getAttributes()) {
                            String variableName = vd.getName();
                            utm.put(variableName, rs.getObject(variableName));
                        }
                        utmList.add(utm);
                    }
                    return new ExecutionResult(utmList);
                }
            }
        }
    }

    @Override
    public void update(Properties properties, WfVariable variable, String condition) throws Exception {
        initParams(properties, variable);
        String columns = "";
        for (VariableDefinition vd : variable.getDefinition().getUserType().getAttributes()) {
            String variableName = vd.getName();
            columns += (columns.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN_SET, variableName,
                    sqlValue(((UserTypeMap) variable.getValue()).get(variableName), vd.getFormatNotNull()));
        }
        executeSql(MessageFormat.format(SQL_UPDATE, getTableName(), columns, condition(condition)));
    }

    private String condition(String condition) {
        if (Strings.isNullOrEmpty(condition)) {
            return SQL_TRUE_SEARCH_CONDITION;
        } else {
            // TODO convert to sql
            return condition;
        }
    }

    @Override
    public void delete(Properties properties, WfVariable variable, String condition) throws Exception {
        initParams(properties, variable);
        executeSql(MessageFormat.format(SQL_DELETE, getTableName(), condition(condition)));
    }

    @Override
    public void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception {
        initParams(properties, variable);
        String columns = "";
        String values = "";
        for (VariableDefinition vd : variable.getDefinition().getUserType().getAttributes()) {
            String variableName = vd.getName();
            columns += (columns.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN, variableName);
            values += (values.length() > 0 ? SQL_LIST_SEPARATOR : "")
                    + sqlValue(((UserTypeMap) variable.getValue()).get(variableName), vd.getFormatNotNull());
        }
        executeSql(MessageFormat.format(SQL_INSERT, getTableName(), columns, values));
    }

    private String sqlValue(Object value, VariableFormat format) {
        if (format instanceof StringFormat) {
            return MessageFormat.format(SQL_VALUE_NVARCHAR, value);
        } else if (format instanceof BooleanFormat) {
            return MessageFormat.format(SQL_VALUE_VARCHAR, value);
        } else if (format instanceof DateFormat) {
            return MessageFormat.format(SQL_VALUE_VARCHAR, FORMAT_DATE.format(value));
        } else if (format instanceof TimeFormat) {
            return MessageFormat.format(SQL_VALUE_VARCHAR, FORMAT_TIME.format(value));
        } else if (format instanceof DateTimeFormat) {
            return MessageFormat.format(SQL_VALUE_VARCHAR, FORMAT_DATETIME.format(value));
        } else {
            return value.toString();
        }
    }

    private boolean checkVariableType(WfVariable variable) {
        VariableDefinition vd = variable.getDefinition();
        UserType[] components = vd.getFormatComponentUserTypes();
        return vd.isUserType() || vd.getFormatNotNull() instanceof ListFormat && components != null && components.length > 0;
    }

    private void initParams(Properties properties, WfVariable variable) throws Exception {
        Preconditions.checkNotNull(properties);
        Preconditions.checkNotNull(variable);
        Preconditions.checkArgument(checkVariableType(variable),
                "Variable '" + variable.getDefinition().getName() + "' must be user type or list of user types.");
        constraints = (IExcelConstraints) properties.get(PROP_CONSTRAINTS);
        fullPath = properties.getProperty(PROP_PATH);
        if (fullPath.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE) || fullPath.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
            String dsName = null;
            if (fullPath.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                dsName = fullPath.substring(DataSourceStuff.PATH_PREFIX_DATA_SOURCE.length());
            } else {
                dsName = (String) variableProvider.getValueNotNull(fullPath.substring(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE.length()));
            }
            ds = (JdbcDataSource) DataSourceStorage.getDataSource(dsName);
            createTableIfNotExist(variable);
        }
    }

    private Connection getConnection() throws Exception {
        String url = ds.getUrl();
        if (ds.getUrl().contains(DataSourceStuff.DATABASE_NAME_MARKER)) {
            url = url.replace(DataSourceStuff.DATABASE_NAME_MARKER, ds.getDbName());
        } else {
            url = url + (ds.getDbType() == JdbcDataSourceType.Oracle ? ':' : '/') + ds.getDbName();
        }
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
        return DriverManager.getConnection(url, ds.getUserName(), ds.getPassword());
    }

    private boolean existOutputParamByVariableName(WfVariable variable) {
        Preconditions.checkNotNull(variable);
        if (variableProvider instanceof ParamBasedVariableProvider) {
            ParamsDef paramsDef = ((ParamBasedVariableProvider) variableProvider).getParamsDef();
            if (paramsDef != null) {
                Map<String, ParamDef> outputParams = paramsDef.getOutputParams();
                if (outputParams != null) {
                    for (Entry<String, ParamDef> entry : outputParams.entrySet()) {
                        String variableName = entry.getValue().getVariableName();
                        if (variable.getDefinition().getName().equals(variableName)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean executeSql(String sql) throws Exception {
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.execute() && ps.getResultSet().next() || ps.getUpdateCount() > 0;
        }
    }

}
