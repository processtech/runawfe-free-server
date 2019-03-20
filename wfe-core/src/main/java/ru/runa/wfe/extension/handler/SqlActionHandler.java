/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.extension.handler;

import com.google.common.io.Closeables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.sqltask.AbstractQuery;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.commons.sqltask.Parameter;
import ru.runa.wfe.commons.sqltask.Result;
import ru.runa.wfe.commons.sqltask.SwimlaneParameter;
import ru.runa.wfe.commons.sqltask.SwimlaneResult;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

/**
 * Executes SQL.
 *
 * @author dofs[197@gmail.com]
 */
public class SqlActionHandler extends AbstractSqlHandler {

    @Autowired
    private ExecutorDao executorDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        super.executeAction(handlerData);
        ExecutionContext executionContext = handlerData.getExecutionContext();
        val variables = new HashMap<String, Object>();
        variables.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, executionContext.getToken().getProcess().getId());
        variables.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
        MapDelegableVariableProvider variableProvider = new MapDelegableVariableProvider(variables, executionContext.getVariableProvider());
        log.debug("all variables: " + variables);
        executeDatabaseTasks(variableProvider, handlerData.getOutputVariables());
    }

    @Override
    protected Map<String, Object> extractResults(VariableProvider variableProvider, ResultSet resultSet, AbstractQuery query) throws Exception {
        val outputVariables = new HashMap<String, Object>();
        if (resultSet != null) {
            for (int i = 0; i < query.getResultVariableCount(); i++) {
                Result result = query.getResultVariable(i);
                String fieldName = result.getFieldName();
                Object newValue = resultSet.getObject(i + 1);
                log.debug("Obtaining result " + fieldName + " from " + newValue);
                if (result instanceof SwimlaneResult) {
                    Actor actor;
                    if ("code".equals(fieldName)) {
                        actor = executorDao.getActorByCode(((Number) newValue).longValue());
                    } else if ("id".equals(fieldName)) {
                        actor = executorDao.getActor(((Number) newValue).longValue());
                    } else {
                        actor = executorDao.getActor(newValue.toString());
                    }
                    newValue = Long.toString(actor.getCode());
                } else if (result.isFieldSetup()) {
                    Object variableValue = variableProvider.getValue(result.getVariableName());
                    if (variableValue == null) {
                        if ("name".equals(result.getFieldName()) || "data".equals(result.getFieldName())
                                || "contentType".equals(result.getFieldName())) {
                            variableValue = new FileVariableImpl("file", "application/octet-stream");
                            ((MapDelegableVariableProvider) variableProvider).add(result.getVariableName(), variableValue);
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
                ((MapDelegableVariableProvider) variableProvider).add(result.getVariableName(), newValue);
                outputVariables.put(result.getVariableName(), newValue);
            }
        }
        return outputVariables;
    }

    @Override
    protected void fillQueryParameters(PreparedStatement ps, VariableProvider variableProvider, AbstractQuery query) throws Exception {
        for (int i = 0; i < query.getParameterCount(); i++) {
            Parameter parameter = query.getParameter(i);
            Object value = variableProvider.getValue(parameter.getVariableName());
            if (parameter instanceof SwimlaneParameter) {
                Actor actor = executorDao.getActorByCode(Long.parseLong((String) value));
                value = PropertyUtils.getProperty(actor, parameter.getFieldName());
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
