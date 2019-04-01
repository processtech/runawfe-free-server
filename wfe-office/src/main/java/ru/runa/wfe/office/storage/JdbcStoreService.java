package ru.runa.wfe.office.storage;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.office.excel.ExcelConstraints;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.ParamBasedVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;

public abstract class JdbcStoreService implements StoreService {

    private static final Log log = LogFactory.getLog(JdbcStoreService.class);

    protected static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    protected static final SimpleDateFormat FORMAT_TIME = new SimpleDateFormat("HH:mm");
    protected static final SimpleDateFormat FORMAT_DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    protected static final String SQL_TABLE_NAME_PREFIX = "SHEET";
    protected static final String SQL_VALUE_NVARCHAR = "N''{0}''";
    protected static final String SQL_VALUE_VARCHAR = "''{0}''";
    protected static final String SQL_VALUE_NULL = "NULL";
    protected static final String SQL_LIST_SEPARATOR = ", ";
    protected static final String SQL_TRUE_SEARCH_CONDITION = "1 = 1";
    protected static final String SQL_AND = " and ";
    protected static final String SQL_CREATE_TABLE = "create table \"{0}\" ({1})";
    protected static final String SQL_COLUMN_DEFINITION = "\"{0}\" {1}";
    protected static final String SQL_INSERT = "insert into \"{0}\" ({1}) values ({2})";
    protected static final String SQL_COLUMN = "\"{0}\"";
    protected static final String SQL_SELECT = "select {0} from \"{1}\" where {2}";
    protected static final String SQL_UPDATE = "update \"{0}\" set {1} where {2}";
    protected static final String SQL_COLUMN_SET = "\"{0}\" = {1}";
    protected static final String SQL_DELETE = "delete from \"{0}\" where {1}";

    private static final String SPACE = " ";
    private static final String LIKE_LITERAL = "LIKE";
    private static final String EQUALS = "=";
    private static final String DOUBLE_EQUALS = "==";

    protected ExcelConstraints constraints;
    protected String fullPath;
    protected VariableProvider variableProvider;
    protected JdbcDataSource ds;

    public JdbcStoreService(VariableProvider variableProvider) {
        this.variableProvider = variableProvider;
    }

    @Override
    public void createFileIfNotExist(String path) throws Exception {
        // Do nothing
    }

    protected UserType userType(WfVariable variable) {
        return variable.getDefinition().isUserType() ? variable.getDefinition().getUserType()
                : variable.getDefinition().getFormatComponentUserTypes()[0];
    }

    protected String tableName() {
        OnSheetConstraints osc = (OnSheetConstraints) constraints;
        String tableName = osc.getSheetName();
        if (Strings.isNullOrEmpty(tableName)) {
            tableName = SQL_TABLE_NAME_PREFIX + osc.getSheetIndex();
        }
        return adjustIdentifier(tableName);
    }

    protected boolean existOutputParamByVariableName(WfVariable variable) {
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

    protected boolean executeSql(String sql) throws Exception {
        log.info(sql);
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.execute() && ps.getResultSet().next() || ps.getUpdateCount() > 0;
        } catch (Exception e) {
            log.error(e);
            throw new JdbcStoreException(e);
        }
    }

    protected String sqlValue(Object value, VariableFormat format) {
        if (value == null) {
            return SQL_VALUE_NULL;
        } else {
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
            } else if (format instanceof ExecutorFormat) {
                return MessageFormat.format(SQL_VALUE_NVARCHAR, ((Executor) value).getName());
            } else if (format instanceof ProcessIdFormat) {
                return MessageFormat.format(SQL_VALUE_VARCHAR, ((Long) value).toString());
            } else if (format instanceof FileFormat) {
                return MessageFormat.format(SQL_VALUE_NVARCHAR, ((FileVariable) value).getName());
            } else {
                return value.toString();
            }
        }
    }

    protected boolean checkVariableType(WfVariable variable) {
        VariableDefinition vd = variable.getDefinition();
        UserType[] components = vd.getFormatComponentUserTypes();
        return vd.isUserType() || vd.getFormatNotNull() instanceof ListFormat && components != null && components.length > 0;
    }

    protected void initParams(Properties properties, WfVariable variable) throws Exception {
        Preconditions.checkNotNull(properties);
        Preconditions.checkNotNull(variable);
        Preconditions.checkArgument(checkVariableType(variable),
                "Variable '" + variable.getDefinition().getName() + "' must be user type or list of user types.");
        constraints = (ExcelConstraints) properties.get(PROP_CONSTRAINTS);
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

    protected void createTableIfNotExist(WfVariable variable) throws Exception {
        String tableName = tableName();
        if (!executeSql(MessageFormat.format(tableExistsSql(), tableName))) {
            String columnDefinitions = "";
            for (VariableDefinition vd : userType(variable).getAttributes()) {
                columnDefinitions += (columnDefinitions.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN_DEFINITION,
                        adjustIdentifier(vd.getName()), typeMap().get(vd.getFormatNotNull().getClass()));
            }
            executeSql(MessageFormat.format(SQL_CREATE_TABLE, tableName, columnDefinitions));
        }
    }

    protected String adjustIdentifier(String identifier) {
        return identifier;
    }

    protected Object adjustValue(Object value) {
        return value;
    }

    abstract protected String tableExistsSql();

    abstract protected Map<Class<? extends VariableFormat>, String> typeMap();

    @Override
    public ExecutionResult findByFilter(Properties properties, WfVariable variable, String condition) throws Exception {
        if (!existOutputParamByVariableName(variable)) {
            throw new WrongParameterException(variable.getDefinition().getName());
        }
        initParams(properties, variable);
        String columns = "";
        UserType ut = variable.getDefinition().getFormatComponentUserTypes()[0];
        for (VariableDefinition vd : ut.getAttributes()) {
            String variableName = adjustIdentifier(vd.getName());
            columns += (columns.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN, variableName);
        }
        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(MessageFormat.format(SQL_SELECT, columns, tableName(), condition(condition)))) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<UserTypeMap> utmList = Lists.newArrayList();
                    while (rs.next()) {
                        UserTypeMap utm = new UserTypeMap(ut);
                        for (VariableDefinition vd : ut.getAttributes()) {
                            String variableName = vd.getName();
                            utm.put(variableName, adjustValue(rs.getObject(adjustIdentifier(variableName))));
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
            columns += (columns.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN_SET, adjustIdentifier(variableName),
                    sqlValue(((UserTypeMap) variable.getValue()).get(variableName), vd.getFormatNotNull()));
        }
        executeSql(MessageFormat.format(SQL_UPDATE, tableName(), columns, condition(condition)));
    }

    private String condition(String condition) {
        if (Strings.isNullOrEmpty(condition)) {
            return SQL_TRUE_SEARCH_CONDITION;
        }
        if (!isConditionValid(condition)) {
            throw new WrongOperatorException(condition);
        }
        condition = ConditionProcessor.hideSpacesInAttributeNames(condition);
        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(condition);
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.startsWith("[") && token.endsWith("]")) {
                sb.append(SPACE);
                sb.append('"').append(token.replace(ConditionProcessor.UNICODE_CHARACTER_OVERLINE, ' ').substring(1, token.length() - 1)).append('"');
            } else if (token.equalsIgnoreCase(LIKE_LITERAL)) {
                sb.append(SPACE);
                sb.append(LIKE_LITERAL);
                sb.append(SPACE);
                sb.append(st.nextToken());
            } else if (token.startsWith("@")) {
                String variableName = token.substring(1);
                String toAppend = "";
                if (variableProvider instanceof ParamBasedVariableProvider) {
                    ParamsDef paramsDef = ((ParamBasedVariableProvider) variableProvider).getParamsDef();
                    if (paramsDef != null) {
                        if (paramsDef.getInputParam(variableName) != null) {
                            Object inputParamValue = paramsDef.getInputParamValue(variableName, variableProvider);
                            toAppend = sqlValue(inputParamValue, variableProvider.getVariable(variableName).getDefinition().getFormatNotNull());
                        } else {
                            WfVariable wfVariable = variableProvider.getVariableNotNull(variableName);
                            toAppend = sqlValue(wfVariable.getValue(), wfVariable.getDefinition().getFormatNotNull());
                        }
                    }
                    sb.append(SPACE);
                    sb.append(toAppend);
                }
            } else if (token.equals(DOUBLE_EQUALS)) {
                sb.append(SPACE);
                sb.append(EQUALS);
            } else {
                sb.append(SPACE);
                sb.append(token);
            }
        }
        return sb.toString();
    }

    @Override
    public void delete(Properties properties, WfVariable variable, String condition) throws Exception {
        initParams(properties, variable);
        executeSql(MessageFormat.format(SQL_DELETE, tableName(), condition(condition)));
    }

    @Override
    public void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception {
        initParams(properties, variable);
        List<UserTypeMap> data = Lists.newArrayList();
        if (variable.getDefinition().isUserType()) {
            data.add((UserTypeMap) variable.getValue());
        } else {
            data.addAll((List<UserTypeMap>) variable.getValue());
        }
        for (UserTypeMap datum : data) {
            String columns = "";
            String values = "";
            for (VariableDefinition vd : datum.getUserType().getAttributes()) {
                String variableName = vd.getName();
                columns += (columns.length() > 0 ? SQL_LIST_SEPARATOR : "") + MessageFormat.format(SQL_COLUMN, adjustIdentifier(variableName));
                values += (values.length() > 0 ? SQL_LIST_SEPARATOR : "") + sqlValue(datum.get(variableName), vd.getFormatNotNull());
            }
            executeSql(MessageFormat.format(SQL_INSERT, tableName(), columns, values));
        }
    }
}
