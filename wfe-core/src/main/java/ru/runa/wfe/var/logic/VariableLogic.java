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
package ru.runa.wfe.var.logic;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Process execution logic.
 * 
 * @author Dofs
 * @since 2.0
 */
public class VariableLogic extends WFCommonLogic {

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException {
        List<WfVariable> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        Map<String, Object> values = variableDAO.getAll(process);
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            Object value = buildVariable(processDefinition, values, variableDefinition);
            if (value != null) {
                result.add(new WfVariable(variableDefinition, value));
            }
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.add(new WfVariable(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private Object buildVariable(ProcessDefinition processDefinition, Map<String, Object> values, VariableDefinition variableDefinition) {
        if (variableDefinition.isUserType()) {
            return buildUserTypeVariable(processDefinition, values, variableDefinition, variableDefinition.getName());
        } else if (ListFormat.class.getName().equals(variableDefinition.getFormatClassName())) {
            return buildListVariable(processDefinition, values, variableDefinition);
        } else {
            return values.remove(variableDefinition.getName());
        }
    }

    private UserTypeMap buildUserTypeVariable(ProcessDefinition processDefinition, Map<String, Object> values, VariableDefinition variableDefinition,
            String prefix) {
        UserTypeMap userTypeMap = new UserTypeMap(variableDefinition);
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String variableName = prefix + UserType.DELIM + attributeDefinition.getName();
            VariableDefinition componentDefinition = new VariableDefinition(variableName, null, attributeDefinition);
            Object value = buildVariable(processDefinition, values, componentDefinition);
            if (value != null) {
                userTypeMap.put(attributeDefinition.getName(), value);
            }
        }
        if (userTypeMap.isEmpty()) {
            return null;
        }
        return userTypeMap;
    }

    private List<Object> buildListVariable(ProcessDefinition processDefinition, Map<String, Object> values, VariableDefinition variableDefinition) {
        List<Object> list = Lists.newArrayList();
        String sizeVariableName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
        Integer size = (Integer) values.remove(sizeVariableName);
        if (size == null) {
            if (values.containsKey(variableDefinition.getName())) {
                Object value = values.remove(variableDefinition.getName());
                if (value instanceof List) {
                    log.debug("Handling back compatibility list value for " + variableDefinition);
                    list = (List<Object>) value;
                    VariableLoader.processComplexVariablesPre430(processDefinition, variableDefinition, null, list);
                } else {
                    log.debug(variableDefinition + " can be changed due to incompatible process definition update");
                    list.add(value);
                }
                return list;
            }
            return null;
        }
        String componentFormat = variableDefinition.getFormatComponentClassNames()[0];
        UserType componentUserType = variableDefinition.getFormatComponentUserTypes()[0];
        for (int i = 0; i < size; i++) {
            String componentName = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                    + VariableFormatContainer.COMPONENT_QUALIFIER_END;
            VariableDefinition componentDefinition = new VariableDefinition(componentName, null, componentFormat, componentUserType);
            Object componentValue = buildVariable(processDefinition, values, componentDefinition);
            list.add(componentValue);
        }
        return list;
    }

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        return executionContext.getVariable(variableName, true);
    }

    public WfVariable getTaskVariable(User user, Long processId, Long taskId, String variableName) {
        Task task = taskDAO.getNotNull(taskId);
        if (task.getIndex() == null) {
            return getVariable(user, processId, variableName);
        }
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        MultiTaskNode node = (MultiTaskNode) processDefinition.getNodeNotNull(task.getNodeId());
        for (VariableMapping mapping : node.getVariableMappings()) {
            if (Objects.equal(mapping.getMappedName(), variableName) || variableName.startsWith(mapping.getMappedName() + UserType.DELIM)) {
                String mappedVariableName = variableName.replaceFirst(mapping.getMappedName(), mapping.getName()
                        + VariableFormatContainer.COMPONENT_QUALIFIER_START + task.getIndex() + VariableFormatContainer.COMPONENT_QUALIFIER_END);
                WfVariable variable = getVariable(user, processId, mappedVariableName);
                if (variable == null) {
                    return null;
                }
                VariableDefinition mappedDefinition = new VariableDefinition(variableName, null, variable.getDefinition());
                return new WfVariable(mappedDefinition, variable.getValue());
            }
        }
        return getVariable(user, processId, variableName);
    }

    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Process process = processDAO.getNotNull(processId);
        // TODO check ProcessPermission.UPDATE
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        processLogDAO.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPDATE_VARIABLES), process, null);
        executionContext.setVariableValues(variables);
    }

}
