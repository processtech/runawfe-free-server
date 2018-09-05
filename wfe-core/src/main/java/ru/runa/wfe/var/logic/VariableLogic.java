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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.val;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentAdminActionLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.ProcessLogFilter;
import ru.runa.wfe.audit.ProcessLogs;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.audit.TaskEndLog;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.audit.logic.AuditLogic;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.logic.WfCommonLogic;
import ru.runa.wfe.execution.ConvertToSimpleVariables;
import ru.runa.wfe.execution.ConvertToSimpleVariablesContext;
import ru.runa.wfe.execution.ConvertToSimpleVariablesResult;
import ru.runa.wfe.execution.ConvertToSimpleVariablesUnrollContext;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionVariableProvider;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dao.VariableLoaderFromMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.dto.WfVariableHistoryState;
import ru.runa.wfe.var.format.VariableFormatContainer;

/**
 * Variables access logic.
 * 
 * @author Dofs
 * @since 2.0
 */
public class VariableLogic extends WfCommonLogic {
    @Autowired
    private AuditLogic auditLogic;
    @Autowired
    private VariableCreator variableCreator;

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException {
        val result = new ArrayList<WfVariable>();
        Process process = processDao.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        permissionDao.checkAllowed(user, Permission.LIST, process);
        Map<Process, Map<String, Variable>> variables = variableDao.getVariables(Collections.singletonList(process));
        val executionContext = new ExecutionContext(processDefinition, process, variables, true);
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            WfVariable variable = executionContext.getVariable(variableDefinition.getName(), false);
            if (variable != null && !Utils.isNullOrEmpty(variable.getValue())) {
                result.add(variable);
            }
        }
        return result;
    }

    public Map<Long, List<WfVariable>> getVariables(User user, List<Long> processIds) throws ProcessDoesNotExistException {
        val result = new HashMap<Long, List<WfVariable>>();
        List<Process> processes = filterSecuredObject(user, processDao.find(processIds), Permission.LIST);
        Map<Process, Map<String, Variable>> variables = variableDao.getVariables(processes);
        for (Process process : processes) {
            val list = new ArrayList<WfVariable>();
            ProcessDefinition processDefinition = getDefinition(process);
            val executionContext = new ExecutionContext(processDefinition, process, variables, true);
            for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
                WfVariable variable = executionContext.getVariable(variableDefinition.getName(), false);
                if (variable != null && !Utils.isNullOrEmpty(variable.getValue())) {
                    list.add(variable);
                }
            }
            result.put(process.getId(), list);
        }
        return result;
    }

    public WfVariableHistoryState getHistoricalVariables(User user, ProcessLogFilter filter) throws ProcessDoesNotExistException {
        if (filter.getCreateDateFrom() == null) {
            return getHistoricalVariableOnDate(user, filter);
        } else {
            return getHistoricalVariableOnRange(user, filter);
        }
    }

    public WfVariableHistoryState getHistoricalVariables(User user, Long processId, Long taskId) throws ProcessDoesNotExistException {
        ProcessLogFilter filter = new ProcessLogFilter();
        filter.setProcessId(processId);
        ProcessLogs processLogs = auditLogic.getProcessLogs(user, filter);
        if (taskId == null || Objects.equal(taskId, 0L)) { // Start process form.
            NodeLeaveLog leaveLog = processLogs.getFirstOrNull(NodeLeaveLog.class);
            if (leaveLog == null) {
                throw new InternalApplicationException("Task " + processId + ", " + taskId + " does not seems completed");
            }
            filter.setCreateDateTo(leaveLog.getCreateDate());
            filter.setIdTo(leaveLog.getId());
            return getHistoricalVariableOnDate(user, filter);
        }
        Date taskCreateDate = null;
        Date taskCompletePressedDate = null;
        Date taskEndDate = null;
        Long tokenId = null;
        for (TaskCreateLog createLog : processLogs.getLogs(TaskCreateLog.class)) {
            if (Objects.equal(createLog.getTaskId(), taskId)) {
                tokenId = createLog.getTokenId();
                break;
            }
        }
        filter.setTokenId(tokenId);
        ProcessLogs tokenLogs = auditLogic.getProcessLogs(user, filter);
        for (BaseProcessLog log : tokenLogs.getLogs()) {
            if (log instanceof TaskCreateLog && Objects.equal(((TaskCreateLog) log).getTaskId(), taskId)) {
                taskCreateDate = log.getCreateDate();
            }
            if (log instanceof VariableLog && taskCreateDate != null && taskCompletePressedDate == null) {
                taskCompletePressedDate = log.getCreateDate();
            }
            if (log instanceof TaskEndLog && Objects.equal(((TaskEndLog) log).getTaskId(), taskId)) {
                taskEndDate = log.getCreateDate();
                break;
            }
        }
        if (taskCreateDate == null) {
            throw new InternalApplicationException("Task " + processId + ", " + taskId + " does not seems started");
        }
        if (taskEndDate == null) {
            throw new InternalApplicationException("Task " + processId + ", " + taskId + " does not seems completed");
        }
        filter = new ProcessLogFilter(processId);
        filter.setCreateDateTo(taskEndDate);
        Calendar dateFrom = CalendarUtil.dateToCalendar(taskCompletePressedDate != null ? taskCompletePressedDate : taskEndDate);
        dateFrom.add(Calendar.MILLISECOND, -100);
        filter.setCreateDateFrom(dateFrom.getTime());
        return getHistoricalVariableOnRange(user, filter);
    }

    // TODO check Permission.LIST on process (but this method is also called from other places).
    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        Process process = processDao.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        return executionContext.getVariable(variableName, true);
    }

    public WfVariable getTaskVariable(User user, Long processId, Long taskId, String variableName) {
        Task task = taskDao.getNotNull(taskId);
        if (task.getIndex() == null) {
            return getVariable(user, processId, variableName);
        }
        Process process = processDao.getNotNull(processId);
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
        CurrentProcess process = currentProcessDao.getNotNull(processId);
        permissionDao.checkAllowed(user, Permission.UPDATE, process);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        processLogDao.addLog(new CurrentAdminActionLog(user.getActor(), CurrentAdminActionLog.ACTION_UPDATE_VARIABLES), process, null);
        executionContext.setVariableValues(variables);
    }

    private WfVariableHistoryState getHistoricalVariableOnRange(User user, ProcessLogFilter filter) {
        val simpleVariablesChanged = new HashSet<String>();
        // Next call is for filling simpleVariablesChanged structure.
        loadSimpleVariablesState(user, processDao.getNotNull(filter.getProcessId()), filter, simpleVariablesChanged);
        Date dateFrom = filter.getCreateDateFrom();
        filter.setCreateDateFrom(null);
        WfVariableHistoryState toState = getHistoricalVariableOnDate(user, filter);
        filter.setCreateDateTo(dateFrom);
        WfVariableHistoryState fromState = getHistoricalVariableOnDate(user, filter);
        return new WfVariableHistoryState(fromState.getVariables(), toState.getVariables(), simpleVariablesChanged);
    }

    private WfVariableHistoryState getHistoricalVariableOnDate(User user, ProcessLogFilter filter) {
        val result = new ArrayList<WfVariable>();
        Process process = processDao.getNotNull(filter.getProcessId());
        permissionDao.checkAllowed(user, Permission.LIST, process);
        val simpleVariablesChanged = new HashSet<String>();
        Map<Process, Map<String, Variable>> processStateOnTime = getProcessStateOnTime(user, process, filter, simpleVariablesChanged);
        VariableLoader loader = new VariableLoaderFromMap(processStateOnTime);
        ProcessDefinition processDefinition = getDefinition(process);
        BaseProcessVariableLoader baseProcessVariableLoader = new BaseProcessVariableLoader(loader, processDefinition, process);
        removeSyncVariablesInBaseProcessMode(processStateOnTime, baseProcessVariableLoader);
        ConvertToSimpleVariables operation = new ConvertToSimpleVariables();
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            String name = variableDefinition.getName();
            WfVariable variable = baseProcessVariableLoader.get(name);
            if (variable != null) {
                ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesUnrollContext(variableDefinition, variable.getValue());
                for (ConvertToSimpleVariablesResult simpleVariable : variableDefinition.getFormatNotNull().processBy(operation, context)) {
                    result.add(new WfVariable(simpleVariable.variableDefinition, simpleVariable.value));
                }
            }
        }
        return new WfVariableHistoryState(Lists.newArrayList(), result, simpleVariablesChanged);
    }

    /**
     * Removes from processes state variables, which is in sync state with base process and BaseProcessMode is on.
     * 
     * @param processStateOnTime
     *            Loaded from history state for process and all it's base processes.
     * @param baseProcessVariableLoader
     *            Component for loading variables with base process variable state support.
     */
    private void removeSyncVariablesInBaseProcessMode(Map<Process, Map<String, Variable>> processStateOnTime,
            BaseProcessVariableLoader baseProcessVariableLoader) {
        ConvertToSimpleVariables operation = new ConvertToSimpleVariables();
        for (val entry : processStateOnTime.entrySet()) {
            Process process = entry.getKey();
            if (!baseProcessVariableLoader.getSubprocessSyncCache().isInBaseProcessIdMode(process)) {
                continue;
            }
            ProcessDefinition processDefinition = getDefinition(process);
            for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
                if (baseProcessVariableLoader.getSubprocessSyncCache().getParentProcessSyncVariableDefinition(process, variableDefinition) == null) {
                    continue;
                }
                WfVariable variable = baseProcessVariableLoader.get(variableDefinition.getName());
                if (variable != null) {
                    ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesUnrollContext(variableDefinition, variable.getValue());
                    for (ConvertToSimpleVariablesResult simpleVariables : variableDefinition.getFormatNotNull().processBy(operation, context)) {
                        entry.getValue().remove(simpleVariables.variableDefinition.getName());
                    }
                }
            }
        }
    }

    /**
     * Load process and all base processes state from logs according to filter.
     * 
     * @param user
     *            Authorized user.
     * @param process
     *            Process for loading process state.
     * @param filter
     *            Filter for loaded process logs.
     * @param simpleVariablesChanged
     *            Simple variables (as it stored in database/logs) names, changed in process.
     * @return Map from process to process variables, loaded according to process filter.
     */
    private Map<Process, Map<String, Variable>> getProcessStateOnTime(User user, Process process, ProcessLogFilter filter,
            Set<String> simpleVariablesChanged) {
        Map<Process, Map<String, Object>> processToVariables = loadSimpleVariablesState(user, process, filter, simpleVariablesChanged);
        val result = new HashMap<Process, Map<String, Variable>>();
        for (val kv : processToVariables.entrySet()) {
            val proc = kv.getKey();
            val vars = kv.getValue();
            val newMap = new HashMap<String, Variable>();
            result.put(proc, newMap);
            for (Process procOrBase = proc; procOrBase != null; procOrBase = getBaseProcess(procOrBase)) {
                ProcessDefinition definition = getDefinition(procOrBase);
                for (val kv2 : vars.entrySet()) {
                    val varName = kv2.getKey();
                    var varValue = kv2.getValue();
                    VariableDefinition variableDefinition = definition.getVariable(varName, false);
                    if (variableDefinition == null) {
                        continue;
                    }
                    if (varValue instanceof String) {
                        varValue = variableDefinition.getFormatNotNull().parse((String) varValue);
                    }
                    Variable variable = variableCreator.create(procOrBase, variableDefinition, varValue);
                    variable.setValue(new ExecutionContext(definition, procOrBase), varValue, variableDefinition);
                    newMap.put(varName, variable);
                }
            }
        }
        return result;
    }

    /**
     * Load simple (as it stored in database/logs) variables state for process and all his base processes.
     * 
     * @param user
     *            Authorized user.
     * @param process
     *            Process for loading variables.
     * @param filter
     *            Filter for loading process logs.
     * @param simpleVariablesChanged
     *            Simple variables (as it stored in database/logs) names, changed in process.
     * @return Map from process to it simple variables state.
     */
    private Map<Process, Map<String, Object>> loadSimpleVariablesState(User user, Process process, ProcessLogFilter filter,
            Set<String> simpleVariablesChanged) {
        Map<Process, Map<String, Object>> processToVariables = Maps.newHashMap();
        for (Process loadingProcess = process; loadingProcess != null; loadingProcess = getBaseProcess(loadingProcess)) {
            processToVariables.put(loadingProcess, loadVariablesForProcessFromLogs(user, loadingProcess, filter, simpleVariablesChanged));
        }
        return processToVariables;
    }

    /**
     * Load simple variables (as it stored in database/logs) for process with specified filter parameters.
     * 
     * @param user
     *            Authorized user.
     * @param process
     *            Process for loading variables.
     * @param filter
     *            Filter for loading process logs.
     * @param simpleVariablesChanged
     *            Simple variables (as it stored in database/logs) names, changed in process.
     * @return Map from simple process variable name to it last known value.
     */
    private Map<String, Object> loadVariablesForProcessFromLogs(User user, Process process, ProcessLogFilter filter,
            Set<String> simpleVariablesChanged) {
        val localFilter = new ProcessLogFilter(filter);
        localFilter.setType(ProcessLog.Type.VARIABLE);
        localFilter.setProcessId(process.getId());
        val result = new HashMap<String, Object>();
        for (VariableLog variableLog : auditLogic.getProcessLogs(user, localFilter).getLogs(VariableLog.class)) {
            String variableName = variableLog.getVariableName();
            if (!(variableLog instanceof VariableCreateLog) || !Utils.isNullOrEmpty((variableLog).getVariableNewValue())) {
                simpleVariablesChanged.add(variableName);
            }
            if (variableLog instanceof VariableDeleteLog) {
                result.remove(variableName);
                continue;
            }
            result.put(variableName, variableLog.getVariableNewValue());
        }
        return result;
    }

    private Process getBaseProcess(Process process) {
        if (Strings.isNullOrEmpty(SystemProperties.getBaseProcessIdVariableName())) {
            return null;
        }
        VariableProvider provider = new ExecutionVariableProvider(new ExecutionContext(getDefinition(process), process));
        final Long baseProcessId = (Long) provider.getValue(SystemProperties.getBaseProcessIdVariableName());
        return baseProcessId != null
                ? processDao.getNotNull(baseProcessId)
                : null;
    }
}
