package ru.runa.wfe.extension.handler;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.sqltask.AbstractQuery;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.commons.sqltask.DatabaseTaskXmlParser;
import ru.runa.wfe.commons.sqltask.Parameter;
import ru.runa.wfe.commons.sqltask.Query;
import ru.runa.wfe.commons.sqltask.Result;
import ru.runa.wfe.commons.sqltask.StoredProcedureQuery;
import ru.runa.wfe.commons.sqltask.SwimlaneParameter;
import ru.runa.wfe.commons.sqltask.SwimlaneResult;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;
import ru.runa.wfe.var.format.ListFormat;

/**
 * Executes SQL.
 *
 * @author dofs[197@gmail.com]
 */
public class SqlActionHandler extends ActionHandlerBase {
    @Autowired
    private ExecutorDao executorDao;

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        Map<String, Object> in = Maps.newHashMap();
        in.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, executionContext.getToken().getProcess().getId());
        in.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
        MapDelegableVariableProvider variableProvider = new MapDelegableVariableProvider(in, executionContext.getVariableProvider());
        DatabaseTask[] databaseTasks = DatabaseTaskXmlParser.parse(configuration, variableProvider);
        log.debug("all variables: " + in);
        Map<String, Object> out = new HashMap<String, Object>();
        Context context = new InitialContext();
        for (int i = 0; i < databaseTasks.length; i++) {
            Connection conn = null;
            try {
                DatabaseTask databaseTask = databaseTasks[i];
                String dsName = databaseTask.getDatasourceName();
                int colonIndex = dsName.indexOf(':');
                if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)
                        || dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
                    if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                        dsName = dsName.substring(colonIndex + 1);
                    } else {
                        dsName = (String) executionContext.getVariableValue(dsName.substring(colonIndex + 1));
                    }
                    JdbcDataSource jds = (JdbcDataSource) DataSourceStorage.getDataSource(dsName);
                    conn = DriverManager.getConnection(DataSourceStuff.adjustUrl(jds), jds.getUserName(), jds.getPassword());
                } else { // jndi
                    if (colonIndex > 0) {
                        if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_JNDI_NAME)) {
                            dsName = dsName.substring(colonIndex + 1);
                        } else {
                            dsName = (String) executionContext.getVariableValue(dsName.substring(colonIndex + 1));
                        }
                    }
                    conn = ((DataSource) context.lookup(dsName)).getConnection();
                }
                for (int j = 0; j < databaseTask.getQueriesCount(); j++) {
                    AbstractQuery query = databaseTask.getQuery(j);
                    PreparedStatement ps;
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
                        ResultSet resultSet = ps.getResultSet();
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
                                    out.put(entry.getKey(), variableValue);
                                }
                                first = false;
                            } else {
                                for (Map.Entry<String, Object> entry : result.entrySet()) {
                                    Object object = out.get(entry.getKey());
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
        // write variables
        executionContext.setVariableValues(out);
    }

    private Map<String, Object> extractResults(MapDelegableVariableProvider in, ResultSet resultSet, AbstractQuery query) throws Exception {
        Map<String, Object> out = new HashMap<String, Object>();
        for (int i = 0; i < query.getResultVariableCount(); i++) {
            Result result = query.getResultVariable(i);
            String fieldName = result.getFieldName();
            Object newValue = resultSet.getObject(i + 1);
            log.debug("Obtaining result " + fieldName + " from " + newValue);
            if (result instanceof SwimlaneResult) {
                Actor actor = null;
                if ("code".equals(fieldName)) {
                    actor = executorDao.getActorByCode(((Number) newValue).longValue());
                } else if ("id".equals(fieldName)) {
                    actor = executorDao.getActor(((Number) newValue).longValue());
                } else {
                    actor = executorDao.getActor(newValue.toString());
                }
                newValue = Long.toString(actor.getCode());
            } else if (result.isFieldSetup()) {
                Object variableValue = in.getValue(result.getVariableName());
                if (variableValue == null) {
                    if ("name".equals(result.getFieldName()) || "data".equals(result.getFieldName()) || "contentType".equals(result.getFieldName())) {
                        variableValue = new FileVariableImpl("file", "application/octet-stream");
                        in.add(result.getVariableName(), variableValue);
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
            in.add(result.getVariableName(), newValue);
            out.put(result.getVariableName(), newValue);
        }
        return out;
    }

    private void fillQueryParameters(PreparedStatement ps, VariableProvider in, AbstractQuery query) throws Exception {
        for (int i = 0; i < query.getParameterCount(); i++) {
            Parameter parameter = query.getParameter(i);
            Object value = in.getValue(parameter.getVariableName());
            if (parameter instanceof SwimlaneParameter) {
                Actor actor = executorDao.getActorByCode(Long.parseLong((String) value));
                value = PropertyUtils.getProperty(actor, ((SwimlaneParameter) parameter).getFieldName());
            } else if (parameter.isFieldSetup()) {
                value = PropertyUtils.getProperty(value, parameter.getFieldName());
            }
            if (value instanceof Date) {
                value = convertDate((Date) value);
            }
            if (value instanceof FileVariable) {
                FileVariable fileVariable = (FileVariable) value;
                if ("name".equals(parameter.getFieldName())) {
                    value = fileVariable.getName();
                } else if ("data".equals(parameter.getFieldName())) {
                    value = fileVariable.getData();
                } else if ("contentType".equals(parameter.getFieldName())) {
                    value = fileVariable.getContentType();
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(fileVariable);
                    oos.close();
                    value = baos.toByteArray();
                }
            }
            int paramIndex = i + 1;
            log.debug("Setting parameter " + paramIndex + " to (" + parameter.getVariableName() + ") = " + value);
            ps.setObject(paramIndex, value);
        }
    }

    private Object convertDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(100, 1, 1);
        if (date.before(calendar.getTime())) {
            calendar.setTime(date);
            calendar.set(calendar.get(Calendar.YEAR) + 2000, calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            date = calendar.getTime();
        }
        return new Timestamp(date.getTime());
    }
}
