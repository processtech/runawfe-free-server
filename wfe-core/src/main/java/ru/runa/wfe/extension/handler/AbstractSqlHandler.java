package ru.runa.wfe.extension.handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.sqltask.AbstractQuery;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.commons.sqltask.DatabaseTaskXmlParser;
import ru.runa.wfe.commons.sqltask.Query;
import ru.runa.wfe.commons.sqltask.StoredProcedureQuery;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;

public abstract class AbstractSqlHandler extends CommonParamBasedHandler {

    protected HandlerData handlerData;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        this.handlerData = handlerData;
    }

    @Override
    public void setConfiguration(String configuration) throws Exception {
        super.setConfiguration(configuration);
        this.configuration = configuration;
    }

    protected abstract void fillQueryParameters(PreparedStatement ps, VariableProvider in, AbstractQuery query) throws Exception;

    protected abstract Map<String, Object> extractResults(VariableProvider variableProvider, ResultSet resultSet, AbstractQuery query)
            throws Exception;

    protected void executeDatabaseTasks(VariableProvider variableProvider, Map<String, Object> outputVariables) throws Exception {
        Context context = new InitialContext();
        DatabaseTask[] databaseTasks = DatabaseTaskXmlParser.parse(configuration, variableProvider);
        for (DatabaseTask databaseTask : databaseTasks) {
            Connection conn = null;
            try {
                String dsName = databaseTask.getDatasourceName();
                int colonIndex = dsName.indexOf(':');
                if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)
                        || dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
                    if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                        dsName = dsName.substring(colonIndex + 1);
                    } else {
                        dsName = (String) variableProvider.getValue(dsName.substring(colonIndex + 1));
                    }
                    JdbcDataSource jds = (JdbcDataSource) DataSourceStorage.getDataSource(dsName);
                    conn = DriverManager.getConnection(DataSourceStuff.adjustUrl(jds), jds.getUserName(), jds.getPassword());
                } else { // jndi
                    if (colonIndex > 0) {
                        if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_JNDI_NAME)) {
                            dsName = dsName.substring(colonIndex + 1);
                        } else {
                            dsName = (String) variableProvider.getValue(dsName.substring(colonIndex + 1));
                        }
                    }
                    conn = ((DataSource) context.lookup(dsName)).getConnection();
                }
                for (int j = 0; j < databaseTask.getQueriesCount(); j++) {
                    AbstractQuery query = databaseTask.getQuery(j);
                    PreparedStatement ps = null;
                    if (query instanceof Query) {
                        log.debug("Preparing query " + query.getSql());
                        ps = conn.prepareStatement(query.getSql());
                    } else if (query instanceof StoredProcedureQuery) {
                        log.debug("Preparing call " + query.getSql());
                        ps = conn.prepareCall(query.getSql());
                    } else {
                        String unknownQueryClassName = query == null ? "null" : query.getClass().getName();
                        throw new Exception("Unknown query type:" + unknownQueryClassName);
                    }
                    fillQueryParameters(ps, variableProvider, query);
                    if (ps.execute()) {
                        final ResultSet resultSet = ps.getResultSet();
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
                    }
                }
            } finally {
                SqlCommons.releaseResources(conn);
            }
        }
    }

}
