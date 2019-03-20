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
package ru.runa.wf.logic.bot;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import org.apache.commons.beanutils.PropertyUtils;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.sqltask.AbstractQuery;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.commons.sqltask.Parameter;
import ru.runa.wfe.commons.sqltask.Result;
import ru.runa.wfe.commons.sqltask.SwimlaneParameter;
import ru.runa.wfe.commons.sqltask.SwimlaneResult;
import ru.runa.wfe.extension.handler.AbstractSqlHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariable;

/**
 * @created on 01.04.2005 as DatabaseTaskHandler
 */
public class SqlTaskHandler extends AbstractSqlHandler {

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        super.executeAction(handlerData);
        Map<String, Object> outputVariables = handlerData.getOutputVariables();
        VariableProvider variableProvider = handlerData.getVariableProvider(); 
        if (variableProvider.getVariable(DatabaseTask.INSTANCE_ID_VARIABLE_NAME) != null) {
            outputVariables.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, handlerData.getTask().getProcessId());
        }
        if (variableProvider.getVariable(DatabaseTask.CURRENT_DATE_VARIABLE_NAME) != null) {
            outputVariables.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
        }
        executeDatabaseTasks(variableProvider, outputVariables);
    }

    @Override
    protected Map<String, Object> extractResults(VariableProvider variableProvider, ResultSet resultSet, AbstractQuery query) throws Exception {
        Map<String, Object> outputVariables = Maps.newHashMap();
        if (resultSet != null) {
            for (int i = 0; i < query.getResultVariableCount(); i++) {
                Result result = query.getResultVariable(i);
                Object newValue = resultSet.getString(i + 1);
                if (result instanceof SwimlaneResult) {
                    String fieldName = result.getFieldName();
                    User user = handlerData.getUser();
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
                    Object variableValue = variableProvider.getValue(result.getVariableName());
                    // diff with SQLActionHandler
                    // if (variableValue == null) {
                    // if ("name".equals(result.getFieldName()) || "data".equals(result.getFieldName()) ||
                    // "contentType".equals(result.getFieldName())) {
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
        }
        return outputVariables;
    }

    @Override
    protected void fillQueryParameters(PreparedStatement ps, VariableProvider variableProvider, AbstractQuery query) throws Exception {
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
            Object value = getVariableValue(handlerData.getUser(), variableProvider, handlerData.getTask(), parameter, variableName);
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
