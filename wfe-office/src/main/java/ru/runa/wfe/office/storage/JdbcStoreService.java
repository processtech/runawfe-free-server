package ru.runa.wfe.office.storage;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.office.excel.ExcelConstraints;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.convert.BaseSqlValueConverter;
import ru.runa.wfe.office.storage.convert.ConverterContext;
import ru.runa.wfe.office.storage.projection.ProjectionModel;
import ru.runa.wfe.office.storage.projection.Sort;
import ru.runa.wfe.var.ParamBasedVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatVisitor;

public abstract class JdbcStoreService implements StoreService {

    private static final Log log = LogFactory.getLog(JdbcStoreService.class);

    protected static final String SQL_TABLE_NAME_PREFIX = "SHEET";
    protected static final String SQL_LIST_SEPARATOR = ", ";
    protected static final String SQL_TRUE_SEARCH_CONDITION = "1 = 1";
    protected static final String SQL_AND = " and ";
    protected static final String SQL_CREATE_TABLE = "create table \"{0}\" ({1})";
    protected static final String SQL_COLUMN_DEFINITION = "\"{0}\" {1}";
    protected static final String SQL_INSERT = "insert into \"{0}\" ({1}) values ({2})";
    protected static final String SQL_COLUMN = "\"{0}\"";
    protected static final String SQL_SELECT = "select {0} from \"{1}\" where {2}";
    protected static final String SQL_SELECT_ORDER_BY = "select {0} from \"{1}\" where {2} order by {3}";
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

    protected String tableName() {
        OnSheetConstraints osc = (OnSheetConstraints) constraints;
        String tableName = osc.getSheetName();
        if (Strings.isNullOrEmpty(tableName)) {
            tableName = SQL_TABLE_NAME_PREFIX + osc.getSheetIndex();
        }
        return adjustIdentifier(tableName);
    }

    protected boolean executeSql(String sql) throws JdbcStoreException {
        log.info(sql);
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.execute() && ps.getResultSet().next() || ps.getUpdateCount() > 0;
        } catch (Exception e) {
            log.error(e);
            throw new JdbcStoreException(e);
        }
    }

    protected String sqlValue(Object value, VariableFormat format) {
        return format.processBy(sqlValueConverter(), new ConverterContext(value));
    }

    protected VariableFormatVisitor<String, ConverterContext> sqlValueConverter() {
        return new BaseSqlValueConverter();
    }

    protected boolean checkVariableType(WfVariable variable) {
        VariableDefinition vd = variable.getDefinition();
        UserType[] components = vd.getFormatComponentUserTypes();
        return vd.isUserType() || vd.getFormatNotNull() instanceof ListFormat && components != null && components.length > 0;
    }

    protected void initParams(Properties properties, UserType userType) throws Exception {
        Preconditions.checkNotNull(properties);
        Preconditions.checkNotNull(userType);
        constraints = (ExcelConstraints) properties.get(PROP_CONSTRAINTS);
        fullPath = properties.getProperty(PROP_PATH);
        ds = (JdbcDataSource) DataSourceStorage.parseDataSource(fullPath, variableProvider);
        if (ds != null) {
            createTableIfNotExist(userType);
        }
    }

    protected void createTableIfNotExist(UserType userType) throws Exception {
        String tableName = tableName();
        if (!executeSql(MessageFormat.format(tableExistsSql(), tableName))) {
            String columnDefinitions = "";
            for (VariableDefinition vd : userType.getAttributes()) {
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
    public ExecutionResult findByFilter(Properties properties, UserType userType, String condition, Iterable<ProjectionModel> projections)
            throws Exception {
        initParams(properties, userType);
        final StringBuilder columns = new StringBuilder();
        for (VariableDefinition vd : userType.getAttributes()) {
            String variableName = adjustIdentifier(vd.getName());
            columns.append(columns.length() > 0 ? SQL_LIST_SEPARATOR : "").append(MessageFormat.format(SQL_COLUMN, variableName));
        }

        final StringBuilder orderBy = new StringBuilder();
        for (ProjectionModel projection : projections) {
            if (projection.getSort() == Sort.NONE) {
                continue;
            }
            final String fieldName = adjustIdentifier(projection.getFieldName());
            orderBy.append(orderBy.length() > 0 ? SQL_LIST_SEPARATOR : "").append(MessageFormat.format(SQL_COLUMN, fieldName)).append(SPACE)
                    .append(projection.getSort().name());
        }

        final String sql = orderBy.length() > 0
                ? MessageFormat.format(SQL_SELECT_ORDER_BY, columns.toString(), tableName(), condition(condition), orderBy.toString())
                : MessageFormat.format(SQL_SELECT, columns.toString(), tableName(), condition(condition));

        try (Connection conn = ds.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                try (ResultSet rs = ps.executeQuery()) {
                    List<UserTypeMap> utmList = Lists.newArrayList();
                    while (rs.next()) {
                        UserTypeMap utm = new UserTypeMap(userType);
                        for (VariableDefinition vd : userType.getAttributes()) {
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
    public ExecutionResult findByFilter(Properties properties, UserType userType, String condition) throws Exception {
        return findByFilter(properties, userType, condition, Collections.emptyList());
    }

    @Override
    public void update(Properties properties, WfVariable variable, String condition) throws Exception {
        Preconditions.checkArgument(checkVariableType(variable),
                "Variable '" + variable.getDefinition().getName() + "' must be user type or list of user types.");
        initParams(properties, userType(variable));
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
                sb.append('"').append(token.replace(ConditionProcessor.UNICODE_CHARACTER_OVERLINE, ' '), 1, token.length() - 1).append('"');
            } else if (token.equalsIgnoreCase(LIKE_LITERAL)) {
                sb.append(SPACE);
                sb.append(LIKE_LITERAL);
                sb.append(SPACE);
                if (st.hasMoreTokens()) {
                    final String nextToken = st.nextToken();
                    if (nextToken.startsWith("@")) {
                        sb.append(extractVariableValue(nextToken));
                    } else {
                        sb.append(nextToken);
                    }
                }
            } else if (token.startsWith("@")) {
                sb.append(SPACE).append(extractVariableValue(token));
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
    public void delete(Properties properties, UserType userType, String condition) throws Exception {
        initParams(properties, userType);
        executeSql(MessageFormat.format(SQL_DELETE, tableName(), condition(condition)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception {
        Preconditions.checkArgument(checkVariableType(variable),
                "Variable '" + variable.getDefinition().getName() + "' must be user type or list of user types.");
        initParams(properties, userType(variable));
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

    private String extractVariableValue(String token) {
        final String variableName = token.substring(1);
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
        } else {
            WfVariable wfVariable = variableProvider.getVariableNotNull(variableName);
            toAppend = sqlValue(wfVariable.getValue(), wfVariable.getDefinition().getFormatNotNull());
        }
        return toAppend;
    }

    protected UserType userType(WfVariable variable) {
        return variable.getDefinition().isUserType() ? variable.getDefinition().getUserType()
                : variable.getDefinition().getFormatComponentUserTypes()[0];
    }

}
