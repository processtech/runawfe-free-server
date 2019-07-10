package ru.runa.wfe.extension.handler.sql;

import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import lombok.val;
import org.apache.commons.beanutils.PropertyUtils;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;
import ru.runa.wfe.var.format.ListFormat;

public abstract class AbstractSqlHandler extends CommonHandler {

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        val variables = new HashMap<String, Object>();
        variables.put("instanceId", variableProvider.getProcessId());
        variables.put("currentDate", new Date());
        Map<String, Object> outputVariables = new HashMap<String, Object>();
        executeSqlQueries(new MapDelegableVariableProvider(variables, variableProvider), outputVariables);
        return outputVariables;
    }

    private void executeSqlQueries(VariableProvider variableProvider, Map<String, Object> outputVariables) throws Exception {
        SqlHandlerConfig sqlHandlerConfig = SqlHandlerConfigXmlParser.parse(configuration, variableProvider);
        Connection connection = null;
        try {
            final String dsValue = sqlHandlerConfig.getDataSourceValue();
            int colonIndex = dsValue.indexOf(':');
            final String dsValueId = dsValue.substring(colonIndex + 1);
            final String dsName;
            if (dsValue.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE) || dsValue.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
                if (dsValue.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                    dsName = dsValueId;
                } else {
                    dsName = (String) variableProvider.getValue(dsValueId);
                }
                JdbcDataSource jdbcDataSource = (JdbcDataSource) DataSourceStorage.getDataSource(dsName);
                connection = jdbcDataSource.getConnection();
            } else { // jndi
                Context context = new InitialContext();
                if (dsValue.startsWith(DataSourceStuff.PATH_PREFIX_JNDI_NAME) || colonIndex == -1) {
                    dsName = dsValueId;
                } else {
                    dsName = (String) variableProvider.getValue(dsValueId);
                }
                connection = ((DataSource) context.lookup(dsName)).getConnection();
            }
            for (SqlHandlerQuery query : sqlHandlerConfig.getQueries()) {
                PreparedStatement preparedStatement;
                if (query.isStoredProcedureQuery()) {
                    log.debug("Preparing call " + query.getSql());
                    preparedStatement = connection.prepareCall(query.getSql());
                } else {
                    log.debug("Preparing query " + query.getSql());
                    preparedStatement = connection.prepareStatement(query.getSql());
                }
                fillQueryParameters(preparedStatement, variableProvider, query);
                if (preparedStatement.execute()) {
                    final ResultSet resultSet = preparedStatement.getResultSet();
                    boolean first = true;
                    while (resultSet.next()) {
                        Map<String, Object> result = extractResults(variableProvider, resultSet, query);
                        if (first) {
                            for (Map.Entry<String, Object> entry : result.entrySet()) {
                                WfVariable variable = variableProvider.getVariableNotNull(entry.getKey());
                                Object variableValue;
                                if (variable.getDefinition().getFormatNotNull() instanceof ListFormat) {
                                    ArrayList<Object> list = new ArrayList<Object>();
                                    list.add(entry.getValue());
                                    variableValue = list;
                                } else {
                                    variableValue = entry.getValue();
                                }
                                outputVariables.put(entry.getKey(), variableValue);
                            }
                            first = false;
                        } else {
                            for (Map.Entry<String, Object> entry : result.entrySet()) {
                                Object object = outputVariables.get(entry.getKey());
                                if (!(object instanceof List)) {
                                    throw new Exception("Variable " + entry.getKey() + " expected to have List<X> format");
                                }
                                ((List<Object>) object).add(entry.getValue());
                            }
                        }
                    }
                } else {
                    log.debug("Updates count: " + preparedStatement.getUpdateCount());
                }
            }
        } finally {
            SqlCommons.releaseResources(connection);
        }
    }

    private void fillQueryParameters(PreparedStatement ps, VariableProvider variableProvider, SqlHandlerQuery query) throws Exception {
        Set<Integer> storedProcesdureOutParametersIndices = Sets.newHashSet();
        for (int i = 0; i < query.getResults().size(); i++) {
            SqlHandlerQueryResult sqlHandlerQueryResult = query.getResults().get(i);
            if (ps instanceof CallableStatement && sqlHandlerQueryResult.getOutParameterIndex() > 0) {
                ((CallableStatement) ps).registerOutParameter(sqlHandlerQueryResult.getOutParameterIndex(), java.sql.Types.VARCHAR);
            }
            storedProcesdureOutParametersIndices.add(sqlHandlerQueryResult.getOutParameterIndex());
        }
        int parameterIndex = 1;
        for (int i = 0; i < query.getParameters().size(); i++) {
            SqlHandlerQueryParameter sqlHandlerQueryParameter = query.getParameters().get(i);
            Object value = getQueryParameterValue(variableProvider, sqlHandlerQueryParameter);
            while (storedProcesdureOutParametersIndices.contains(parameterIndex)) {
                log.debug("Parameter " + parameterIndex + " identified as output for stored procedure");
                parameterIndex++;
            }
            log.debug("Setting input parameter " + parameterIndex + " (" + sqlHandlerQueryParameter.getVariableName() + ") to " + value + " of type "
                    + (value != null ? value.getClass() : "NULL"));
            ps.setObject(parameterIndex, value);
            parameterIndex++;
        }
    }

    private Object getQueryParameterValue(VariableProvider variableProvider, SqlHandlerQueryParameter sqlHandlerQueryParameter) throws Exception {
        Object value = variableProvider.getValue(sqlHandlerQueryParameter.getVariableName());
        if (sqlHandlerQueryParameter.isSwimlane()) {
            value = TypeConversionUtil.convertToExecutor(value, ((AbstractVariableProvider) variableProvider).getExecutorLoader());
        }
        if (sqlHandlerQueryParameter.getFieldName() != null) {
            value = PropertyUtils.getProperty(value, sqlHandlerQueryParameter.getFieldName());
        }
        if (value instanceof FileVariable) {
            // this is default path for devstudio configurer
            FileVariable fileVariable = unproxyFileVariable((FileVariable) value);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(fileVariable);
            oos.close();
            value = baos.toByteArray();
        }
        return value;
    }

    protected FileVariable unproxyFileVariable(FileVariable fileVariable) {
        return fileVariable;
    }

    private Map<String, Object> extractResults(VariableProvider variableProvider, ResultSet resultSet, SqlHandlerQuery query) throws Exception {
        val outputVariables = new HashMap<String, Object>();
        for (int i = 0; i < query.getResults().size(); i++) {
            SqlHandlerQueryResult sqlHandlerQueryResult = query.getResults().get(i);
            String fieldName = sqlHandlerQueryResult.getFieldName();
            Object newValue = resultSet.getObject(i + 1);
            log.debug("Obtaining result " + fieldName + " from " + newValue);
            if (sqlHandlerQueryResult.isSwimlane()) {
                Object actorValue;
                if ("id".equals(fieldName)) {
                    actorValue = "ID" + newValue;
                } else {
                    actorValue = newValue;
                }
                Actor actor = TypeConversionUtil.convertToExecutor(actorValue, ((AbstractVariableProvider) variableProvider).getExecutorLoader());
                newValue = Long.toString(actor.getCode());
            } else if (fieldName != null) {
                Object variableValue = variableProvider.getValue(sqlHandlerQueryResult.getVariableName());
                if (variableValue == null) {
                    if ("name".equals(sqlHandlerQueryResult.getFieldName()) || "data".equals(sqlHandlerQueryResult.getFieldName()) || "contentType".equals(sqlHandlerQueryResult.getFieldName())) {
                        variableValue = new FileVariableImpl("file", "application/octet-stream");
                        ((MapDelegableVariableProvider) variableProvider).add(sqlHandlerQueryResult.getVariableName(), variableValue);
                    }
                }
                PropertyUtils.setProperty(variableValue, fieldName, newValue);
                newValue = variableValue;
            }
            if (newValue instanceof Blob) {
                ObjectInputStream ois = new ObjectInputStream(((Blob) newValue).getBinaryStream());
                newValue = ois.readObject();
                Closeables.closeQuietly(ois);
            }
            if (newValue instanceof byte[]) {
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) newValue);
                ObjectInputStream ois = new ObjectInputStream(bais);
                newValue = ois.readObject();
                Closeables.closeQuietly(ois);
            }
            ((MapDelegableVariableProvider) variableProvider).add(sqlHandlerQueryResult.getVariableName(), newValue);
            outputVariables.put(sqlHandlerQueryResult.getVariableName(), newValue);
        }
        return outputVariables;
    }

}
