package ru.runa.wf.logic.bot;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import org.apache.commons.beanutils.PropertyUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.TypeConversionUtil;
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
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.ListFormat;

public class DatabaseTaskHandler extends TaskHandlerBase {

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        Map<String, Object> outputVariables = Maps.newHashMap();
        if (variableProvider.getVariable(DatabaseTask.INSTANCE_ID_VARIABLE_NAME) != null) {
            outputVariables.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, task.getProcessId());
        }
        if (variableProvider.getVariable(DatabaseTask.CURRENT_DATE_VARIABLE_NAME) != null) {
            outputVariables.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
        }
        DatabaseTask[] databaseTasks = DatabaseTaskXmlParser.parse(configuration, variableProvider);
        executeDatabaseTasks(user, variableProvider, task, outputVariables, databaseTasks);
        return outputVariables;
    }

    @SuppressWarnings("unchecked")
    private void executeDatabaseTasks(User user, VariableProvider variableProvider, WfTask task, Map<String, Object> outputVariables,
            DatabaseTask[] databaseTasks) throws Exception {
        Context context = new InitialContext();
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
                    try {
                        if (query instanceof Query) {
                            ps = conn.prepareStatement(query.getSql());
                        } else if (query instanceof StoredProcedureQuery) {
                            final CallableStatement cps = conn.prepareCall(query.getSql());
                            ps = cps;
                            fillQueryParameters(user, ps, variableProvider, query, task);
                            cps.executeUpdate();
                            Map<String, Object> result = extractResultsToProcessVariables(user, variableProvider, new Function<Integer, Object>() {
                                @Override
                                public Object apply(Integer input) {
                                    try {
                                        return cps.getObject(input);
                                    } catch (SQLException e) {
                                        throw new InternalApplicationException(e);
                                    }
                                }
                            }, query);
                            outputVariables.putAll(result);
                            return;
                        } else {
                            String unknownQueryClassName = query == null ? "null" : query.getClass().getName();
                            throw new Exception("Unknown query type:" + unknownQueryClassName);
                        }
                        fillQueryParameters(user, ps, variableProvider, query, task);
                        if (ps.execute()) {
                            final ResultSet resultSet = ps.getResultSet();
                            boolean first = true;
                            while (resultSet.next()) {
                                Map<String, Object> result = extractResultsToProcessVariables(user, variableProvider,
                                        new Function<Integer, Object>() {
                                            @Override
                                            public Object apply(Integer input) {
                                                try {
                                                    return resultSet.getObject(input);
                                                } catch (SQLException e) {
                                                    throw new InternalApplicationException(e);
                                                }
                                            }
                                        }, query);
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
                    } finally {
                        SqlCommons.releaseResources(ps);
                    }
                }
            } finally {
                SqlCommons.releaseResources(conn);
            }
        }
    }

    private Map<String, Object> extractResultsToProcessVariables(User user, VariableProvider variableProvider,
            Function<Integer, Object> getValueAtIndex, AbstractQuery query) throws Exception {
        Map<String, Object> outputVariables = Maps.newHashMap();
        for (int i = 0; i < query.getResultVariableCount(); i++) {
            Result result = query.getResultVariable(i);
            int resultIndex = result.getOutParameterIndex() <= 0 ? i + 1 : result.getOutParameterIndex();
            Object newValue = getValueAtIndex.apply(resultIndex);
            Object variableValue = variableProvider.getValue(result.getVariableName());
            if (result instanceof SwimlaneResult) {
                String fieldName = result.getFieldName();
                Actor actor = null;
                if ("code".equals(fieldName)) {
                    actor = TypeConversionUtil.convertToExecutor(newValue, new DelegateExecutorLoader(user));
                } else if ("id".equals(fieldName)) {
                    actor = Delegates.getExecutorService().getExecutor(user, (Long) newValue);
                } else {
                    actor = Delegates.getExecutorService().getExecutorByName(user, (String) newValue);
                }
                newValue = Long.toString(actor.getCode());
            } else if (result.isFieldSetup()) {
                // diff with SQLActionHandler
                // if (variableValue == null) {
                // if ("name".equals(result.getFieldName()) || "data".equals(result.getFieldName()) || "contentType".equals(result.getFieldName())) {
                // variableValue = new FileVariable("file", "application/octet-stream");
                // variableProvider.add(result.getVariableName(), variableValue);
                // }
                // }
                PropertyUtils.setProperty(variableValue, result.getFieldName(), newValue);
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
            outputVariables.put(result.getVariableName(), newValue);
        }
        return outputVariables;
    }

    private void fillQueryParameters(User user, PreparedStatement ps, VariableProvider variableProvider, AbstractQuery query, WfTask task)
            throws Exception {
        Set<Integer> outParamIdx = Sets.newHashSet();
        for (int i = 0; i < query.getResultVariableCount(); i++) {
            if (ps instanceof CallableStatement && query.getResultVariable(i).getOutParameterIndex() > 0) {
                ((CallableStatement) ps).registerOutParameter(query.getResultVariable(i).getOutParameterIndex(), java.sql.Types.VARCHAR);
            }
            outParamIdx.add(query.getResultVariable(i).getOutParameterIndex());
        }
        int parameterIndex = 1;
        for (int i = 0; i < query.getParameterCount(); i++) {
            Parameter parameter = query.getParameter(i);
            String variableName = parameter.getVariableName();
            Object value = getVariableValue(user, variableProvider, task, parameter, variableName);
            while (outParamIdx.contains(parameterIndex)) {
                ++parameterIndex;
            }
            ps.setObject(parameterIndex, value);
            ++parameterIndex;
        }
    }

    private Object getVariableValue(User user, VariableProvider variableProvider, WfTask task, Parameter parameter, String variableName)
            throws Exception {
        Object value = variableProvider.getValue(variableName);
        if (value == null) {
            if (DatabaseTask.INSTANCE_ID_VARIABLE_NAME.equals(variableName)) {
                value = task.getProcessId();
            }
            if (DatabaseTask.CURRENT_DATE_VARIABLE_NAME.equals(variableName)) {
                value = new Date();
            }
        }
        if (parameter instanceof SwimlaneParameter) {
            Actor actor = TypeConversionUtil.convertToExecutor(value, new DelegateExecutorLoader(user));
            value = PropertyUtils.getProperty(actor, ((SwimlaneParameter) parameter).getFieldName());
        } else if (parameter.isFieldSetup()) {
            value = PropertyUtils.getProperty(value, parameter.getFieldName());
        }
        // diff with SQLActionHandler
        // if (value instanceof Date) {
        // value = convertDate((Date) value);
        // }
        if (value instanceof FileVariableProxy) {
            value = Delegates.getExecutionService().getFileVariableValue(user, task.getProcessId(), variableName);
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
        return value;
    }

}
