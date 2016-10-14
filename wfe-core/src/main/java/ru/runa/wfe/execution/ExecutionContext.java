/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.execution;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.dao.NodeProcessDAO;
import ru.runa.wfe.execution.dao.ProcessDAO;
import ru.runa.wfe.execution.dao.SwimlaneDAO;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.MultiSubprocessNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dao.VariableDAO;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ExecutionContext {
    private static Log log = LogFactory.getLog(ExecutionContext.class);
    private final ProcessDefinition processDefinition;
    private final Token token;
    private Task task;
    private final Map<String, Object> transientVariables = Maps.newHashMap();
    private final SubprocessSyncCache subprocessSyncCache = new SubprocessSyncCache();
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private VariableCreator variableCreator;
    @Autowired
    private ProcessDAO processDAO;
    @Autowired
    private NodeProcessDAO nodeProcessDAO;
    @Autowired
    private ProcessLogDAO processLogDAO;
    @Autowired
    private VariableDAO variableDAO;
    @Autowired
    private TaskDAO taskDAO;
    @Autowired
    private JobDAO jobDAO;
    @Autowired
    private SwimlaneDAO swimlaneDAO;

    private final VariableLoader variableLoader;

    protected ExecutionContext(ApplicationContext applicationContext, ProcessDefinition processDefinition, Token token,
            Map<Process, Map<String, Variable<?>>> loadedVariables) {
        this.processDefinition = processDefinition;
        this.token = token;
        Preconditions.checkNotNull(token, "token");
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
        this.variableLoader = new VariableLoader(variableDAO, loadedVariables);
    }

    public ExecutionContext(ProcessDefinition processDefinition, Token token, Map<Process, Map<String, Variable<?>>> loadedVariables) {
        this(ApplicationContextFactory.getContext(), processDefinition, token, loadedVariables);
    }

    public ExecutionContext(ProcessDefinition processDefinition, Token token) {
        this(ApplicationContextFactory.getContext(), processDefinition, token, null);
    }

    public ExecutionContext(ProcessDefinition processDefinition, Process process, Map<Process, Map<String, Variable<?>>> loadedVariables) {
        this(processDefinition, process.getRootToken(), loadedVariables);
    }

    public ExecutionContext(ProcessDefinition processDefinition, Process process) {
        this(processDefinition, process.getRootToken());
    }

    public ExecutionContext(ProcessDefinition processDefinition, Task task) {
        this(processDefinition, task.getToken());
        this.task = task;
    }

    /**
     * retrieves the transient variable for the given name.
     */
    public Object getTransientVariable(String name) {
        return transientVariables.get(name);
    }

    /**
     * sets the transient variable for the given name to the given value.
     */
    public void setTransientVariable(String name, Object value) {
        transientVariables.put(name, value);
    }

    public Node getNode() {
        return getToken().getNodeNotNull(getProcessDefinition());
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public Process getProcess() {
        Process process = getToken().getProcess();
        Preconditions.checkNotNull(process, "process");
        return process;
    }

    public Token getToken() {
        return token;
    }

    /**
     * @return task or <code>null</code>
     */
    public Task getTask() {
        return task;
    }

    public NodeProcess getParentNodeProcess() {
        return nodeProcessDAO.findBySubProcessId(getProcess().getId());
    }

    public List<Process> getTokenSubprocesses() {
        return nodeProcessDAO.getSubprocesses(getToken());
    }

    public List<Process> getSubprocesses() {
        return nodeProcessDAO.getSubprocesses(getProcess());
    }

    public List<Process> getNotEndedSubprocesses() {
        return nodeProcessDAO.getSubprocesses(getProcess(), getToken().getNodeId(), getToken(), false);
    }

    public List<Process> getSubprocessesRecursively() {
        return nodeProcessDAO.getSubprocessesRecursive(getProcess());
    }

    /**
     * @return the variable value with the given name.
     */
    public WfVariable getVariable(String name, boolean searchInSwimlanes) {
        if (searchInSwimlanes) {
            SwimlaneDefinition swimlaneDefinition = getProcessDefinition().getSwimlane(name);
            if (swimlaneDefinition != null) {
                Swimlane swimlane = swimlaneDAO.findByProcessAndName(getProcess(), swimlaneDefinition.getName());
                if (swimlane == null && SystemProperties.isSwimlaneAutoInitializationEnabled()) {
                    swimlane = swimlaneDAO.findOrCreateInitialized(this, swimlaneDefinition, false);
                }
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), swimlane != null ? swimlane.getExecutor() : null);
            }
        }
        WfVariable variable = variableLoader.getVariable(getProcessDefinition(), getProcess(), name);
        if (variable == null || Utils.isNullOrEmpty(variable.getValue())
                || Objects.equal(variable.getDefinition().getDefaultValue(), variable.getValue()) || variable.getValue() instanceof UserTypeMap) {
            variable = getVariableUsingBaseProcess(getProcessDefinition(), getProcess(), name, variable);
        }
        if (variable != null) {
            return variable;
        }
        if (SystemProperties.isV3CompatibilityMode() || SystemProperties.isAllowedNotDefinedVariables()) {
            Variable<?> dbVariable = variableLoader.get(getProcess(), name);
            return new WfVariable(name, dbVariable != null ? dbVariable.getValue() : null);
        }
        log.debug("No variable defined by '" + name + "' in " + getProcess() + ", returning null");
        return null;
    }

    /**
     * @return the variable or swimlane value with the given name.
     */
    public Object getVariableValue(String name) {
        WfVariable variable = getVariable(name, true);
        if (variable != null) {
            return variable.getValue();
        }
        return null;
    }

    public void setVariableValue(String name, Object value) {
        Preconditions.checkNotNull(name, "name");
        SwimlaneDefinition swimlaneDefinition = getProcessDefinition().getSwimlane(name);
        if (swimlaneDefinition != null) {
            log.debug("Assigning swimlane '" + name + "' value '" + value + "'");
            Swimlane swimlane = swimlaneDAO.findOrCreate(getProcess(), swimlaneDefinition);
            swimlane.assignExecutor(this, TypeConversionUtil.convertTo(Executor.class, value), true);
            return;
        }
        VariableDefinition variableDefinition = getProcessDefinition().getVariable(name, false);
        if (variableDefinition == null) {
            if (SystemProperties.isAllowedNotDefinedVariables()) {
                variableDefinition = new VariableDefinition(name, null);
            } else {
                throw new InternalApplicationException("Variable '" + name
                        + "' is not defined in process definition and setting 'undefined.variables.allowed'=false");
            }
        }
        setVariableValue(variableDefinition, value);
    }

    /**
     * Adds all the given variables. It doesn't remove any existing variables unless they are overwritten by the given variables.
     */
    public void setVariableValues(Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariableValue(entry.getKey(), entry.getValue());
        }
    }

    public IVariableProvider getVariableProvider() {
        return new ExecutionVariableProvider(this);
    }

    public void addLog(ProcessLog processLog) {
        processLogDAO.addLog(processLog, getProcess(), token);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("processId", getToken().getProcess().getId()).add("tokenId", getToken().getId()).toString();
    }

    private WfVariable getVariableUsingBaseProcess(ProcessDefinition processDefinition, Process process, String name, WfVariable variable) {
        Long baseProcessId = subprocessSyncCache.getBaseProcessId(processDefinition, process);
        if (baseProcessId != null) {
            name = subprocessSyncCache.getBaseProcessReadVariableName(processDefinition, process, name);
            if (name != null) {
                log.debug("Loading variable '" + name + "' from process '" + baseProcessId + "'");
                Process baseProcess = processDAO.getNotNull(baseProcessId);
                ProcessDefinition baseProcessDefinition = processDefinitionLoader.getDefinition(baseProcess);
                WfVariable baseVariable = variableLoader.getVariable(baseProcessDefinition, baseProcess, name);
                if (variable != null && variable.getValue() instanceof UserTypeMap && baseVariable != null
                        && baseVariable.getValue() instanceof UserTypeMap) {
                    ((UserTypeMap) variable.getValue()).merge((UserTypeMap) baseVariable.getValue(), false);
                } else if (baseVariable != null) {
                    if (!Utils.isNullOrEmpty(baseVariable.getValue())) {
                        return baseVariable;
                    }
                    return getVariableUsingBaseProcess(baseProcessDefinition, baseProcess, name, baseVariable);
                }
                return getVariableUsingBaseProcess(baseProcessDefinition, baseProcess, name, variable);
            }
        }
        return variable;
    }

    private void setVariableValue(VariableDefinition variableDefinition, Object value) {
        Preconditions.checkNotNull(variableDefinition, "variableDefinition");
        if (!SystemProperties.isV3CompatibilityMode()) {
            if (value != null && variableDefinition != null && SystemProperties.isStrongVariableFormatEnabled()) {
                Class<?> definedClass = variableDefinition.getFormatNotNull().getJavaClass();
                if (!definedClass.isAssignableFrom(value.getClass())) {
                    if (SystemProperties.isVariableAutoCastingEnabled()) {
                        try {
                            value = TypeConversionUtil.convertTo(definedClass, value);
                        } catch (Exception e) {
                            throw new InternalApplicationException("Variable '" + variableDefinition.getName() + "' defined as '" + definedClass
                                    + "' but value is instance of '" + value.getClass() + "'", e);
                        }
                    } else {
                        throw new InternalApplicationException("Variable '" + variableDefinition.getName() + "' defined as '" + definedClass
                                + "' but value is instance of '" + value.getClass() + "'");
                    }
                }
            }
        }
        // user type variables support
        if (value instanceof UserTypeMap) {
            UserTypeMap userTypeMap = (UserTypeMap) value;
            Map<VariableDefinition, Object> expanded = userTypeMap.expandAttributes(variableDefinition.getName());
            for (Map.Entry<VariableDefinition, Object> entry : expanded.entrySet()) {
                setVariableValue(entry.getKey(), entry.getValue());
            }
            return;
        }
        if (value == null && variableDefinition.getUserType() != null) {
            for (VariableDefinition definition : variableDefinition.expandUserType(false)) {
                setVariableValue(definition, null);
            }
            return;
        }
        // list variables support
        if (ListFormat.class.getName().equals(variableDefinition.getFormatClassName())) {
            int newSize = TypeConversionUtil.getListSize(value);
            String sizeVariableName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
            WfVariable oldSizeVariable = getVariable(sizeVariableName, false);
            int maxSize = newSize;
            if (oldSizeVariable != null && oldSizeVariable.getValue() instanceof Integer) {
                maxSize = Math.max((Integer) oldSizeVariable.getValue(), newSize);
            }
            VariableDefinition sizeDefinition = new VariableDefinition(sizeVariableName, null, LongFormat.class.getName(), null);
            setSimpleVariableValue(getProcessDefinition(), getToken(), sizeDefinition, value != null ? newSize : null);
            String[] formatComponentClassNames = variableDefinition.getFormatComponentClassNames();
            String componentFormat = formatComponentClassNames.length > 0 ? formatComponentClassNames[0] : null;
            UserType[] formatComponentUserTypes = variableDefinition.getFormatComponentUserTypes();
            UserType componentUserType = formatComponentUserTypes.length > 0 ? formatComponentUserTypes[0] : null;
            List<?> list = (List<?>) value;
            for (int i = 0; i < maxSize; i++) {
                String name = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                VariableDefinition definition = new VariableDefinition(name, null, componentFormat, componentUserType);
                Object object = list != null && list.size() > i ? list.get(i) : null;
                setVariableValue(definition, object);
            }
            if (SystemProperties.isV4ListVariableCompatibilityMode()) {
                // delete old list variables as blobs (pre 4.3.0)
                Variable<?> variable = variableLoader.get(getProcess(), variableDefinition.getName());
                if (variable != null) {
                    log.debug("Removing old-style list variable '" + variableDefinition.getName() + "'");
                    variableDAO.delete(variable);
                }
            }
            return;
        }
        setSimpleVariableValue(getProcessDefinition(), getToken(), variableDefinition, value);
    }

    private VariableLog setSimpleVariableValue(ProcessDefinition processDefinition, Token token, VariableDefinition variableDefinition, Object value) {
        VariableLog resultingVariableLog = null;
        Variable<?> variable = variableLoader.get(token.getProcess(), variableDefinition.getName());
        // if there is exist variable and it doesn't support the current type
        if (variable != null && !variable.supports(value)) {
            log.debug("Variable type is changing: deleting old variable '" + variableDefinition.getName() + "' in " + token.getProcess());
            variableDAO.delete(variable);
            resultingVariableLog = new VariableDeleteLog(variable);
            variable = null;
        }
        if (variable == null) {
            VariableDefinition syncVariableDefinition = subprocessSyncCache.getParentProcessSyncVariableDefinition(processDefinition,
                    token.getProcess(), variableDefinition);
            if (syncVariableDefinition != null) {
                Token parentToken = subprocessSyncCache.getParentProcessToken(token.getProcess());
                ProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(parentToken.getProcess());
                log.debug("Setting " + token.getProcess().getId() + "." + variableDefinition.getName() + " in parent process "
                        + parentToken.getProcess().getId() + "." + syncVariableDefinition.getName());
                VariableLog parentVariableLog = setSimpleVariableValue(parentProcessDefinition, parentToken, syncVariableDefinition, value);
                if (parentVariableLog != null) {
                    VariableLog markingVariableLog = parentVariableLog.getContentCopy();
                    markingVariableLog.setVariableName(variableDefinition.getName());
                    resultingVariableLog = markingVariableLog;
                }
            }
            if (value != null) {
                if (syncVariableDefinition == null || !subprocessSyncCache.isInBaseProcessIdMode(token.getProcess())) {
                    variable = variableCreator.create(token.getProcess(), variableDefinition, value);
                    resultingVariableLog = variable.setValue(this, value, variableDefinition.getFormatNotNull());
                    variableDAO.create(variable);
                    if (variableDefinition.getName().contains(VariableFormatContainer.COMPONENT_QUALIFIER_START)) {
                        String autoExtendVariableName = variableDefinition.getName();
                        while (autoExtendVariableName.contains(VariableFormatContainer.COMPONENT_QUALIFIER_START)
                                && autoExtendVariableName.contains(VariableFormatContainer.COMPONENT_QUALIFIER_END)) {
                            int listIndexStart = autoExtendVariableName.lastIndexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                            int listIndexEnd = autoExtendVariableName.lastIndexOf(VariableFormatContainer.COMPONENT_QUALIFIER_END);
                            String listVariableName = autoExtendVariableName.substring(0, listIndexStart);
                            String sizeVariableName = listVariableName + VariableFormatContainer.SIZE_SUFFIX;
                            VariableDefinition sizeDefinition = new VariableDefinition(sizeVariableName, null, LongFormat.class.getName(), null);
                            Integer oldSize = (Integer) variableLoader.getVariableValue(processDefinition, token.getProcess(), sizeDefinition);
                            int listIndex = Integer.parseInt(autoExtendVariableName.substring(listIndexStart
                                    + VariableFormatContainer.COMPONENT_QUALIFIER_START.length(), listIndexEnd));
                            int newSize = listIndex + 1;
                            if (oldSize == null || oldSize.intValue() < newSize) {
                                log.debug("Auto-extending list " + listVariableName + " size: " + oldSize + " -> " + newSize);
                                setSimpleVariableValue(processDefinition, token, sizeDefinition, newSize);
                            }
                            autoExtendVariableName = listVariableName;
                        }
                    }
                }
            }
        } else {
            if (Objects.equal(value, variable.getValue())) {
                // order is valuable due to Timestamp.equals implementation
                return null;
            }
            if (ApplicationContextFactory.getDBType() == DBType.ORACLE && Utils.isNullOrEmpty(value) && Utils.isNullOrEmpty(variable.getValue())) {
                // ignore changes "" -> " " for Oracle
                return null;
            }
            log.debug("Updating variable '" + variableDefinition.getName() + "' in '" + getProcess() + "' to '" + value + "'"
                    + (value != null ? " of " + value.getClass() : ""));
            resultingVariableLog = variable.setValue(this, value, variableDefinition.getFormatNotNull());
            VariableDefinition syncVariableDefinition = subprocessSyncCache.getParentProcessSyncVariableDefinition(processDefinition,
                    token.getProcess(), variableDefinition);
            if (syncVariableDefinition != null) {
                Token parentToken = subprocessSyncCache.getParentProcessToken(token.getProcess());
                ProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(parentToken.getProcess());
                log.debug("Setting " + token.getProcess().getId() + "." + variableDefinition.getName() + " in parent process "
                        + parentToken.getProcess().getId() + "." + syncVariableDefinition.getName());
                setSimpleVariableValue(parentProcessDefinition, parentToken, syncVariableDefinition, value);
            }
        }
        if (value instanceof Date) {
            updateRelatedObjectsDueToDateVariableChange(variableDefinition.getName());
        }
        if (resultingVariableLog != null) {
            processLogDAO.addLog(resultingVariableLog, token.getProcess(), token);
        }
        return resultingVariableLog;
    }

    private void updateRelatedObjectsDueToDateVariableChange(String variableName) {
        List<Task> tasks = taskDAO.findByProcessAndDeadlineExpressionContaining(getProcess(), variableName);
        for (Task task : tasks) {
            Date oldDate = task.getDeadlineDate();
            task.setDeadlineDate(ExpressionEvaluator.evaluateDueDate(getVariableProvider(), task.getDeadlineDateExpression()));
            log.info(String.format("Changed deadlineDate for %s from %s to %s", task, oldDate, task.getDeadlineDate()));
        }
        List<Job> jobs = jobDAO.findByProcessAndDeadlineExpressionContaining(getProcess(), variableName);
        for (Job job : jobs) {
            Date oldDate = job.getDueDate();
            job.setDueDate(ExpressionEvaluator.evaluateDueDate(getVariableProvider(), job.getDueDateExpression()));
            log.info(String.format("Changed dueDate for %s from %s to %s", job, oldDate, job.getDueDate()));
        }
    }

    private class SubprocessSyncCache {
        private Map<Process, NodeProcess> subprocessesInfoMap = Maps.newHashMap();
        private Map<Process, Boolean> baseProcessIdModesMap = Maps.newHashMap();
        private Map<Process, Boolean> multiSubprocessFlagsMap = Maps.newHashMap();
        private Map<Process, Map<String, String>> readVariableNamesMap = Maps.newHashMap();
        private Map<Process, Map<String, String>> syncVariableNamesMap = Maps.newHashMap();
        private Map<Process, Long> baseProcessIdsMap = Maps.newHashMap();

        private Long getBaseProcessId(ProcessDefinition processDefinition, Process process) {
            if (!baseProcessIdsMap.containsKey(process)) {
                String baseProcessIdVariableName = SystemProperties.getBaseProcessIdVariableName();
                if (baseProcessIdVariableName != null && processDefinition.getVariable(baseProcessIdVariableName, false) != null) {
                    WfVariable baseProcessIdVariable = variableLoader.getVariable(processDefinition, process, baseProcessIdVariableName);
                    Long baseProcessId = (Long) (baseProcessIdVariable != null ? baseProcessIdVariable.getValue() : null);
                    if (Objects.equal(baseProcessId, process.getId())) {
                        throw new InternalApplicationException(baseProcessIdVariableName + " reference should not point to current process id "
                                + process.getId());
                    }
                    baseProcessIdsMap.put(process, baseProcessId);
                }
            }
            return baseProcessIdsMap.get(process);
        }

        private NodeProcess getSubprocessNodeInfo(Process process) {
            if (!subprocessesInfoMap.containsKey(process)) {
                NodeProcess nodeProcess = nodeProcessDAO.findBySubProcessId(process.getId());
                if (nodeProcess != null) {
                    Map<String, String> readVariableNames = Maps.newHashMap();
                    Map<String, String> syncVariableNames = Maps.newHashMap();
                    ProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(nodeProcess.getProcess());
                    Node node = parentProcessDefinition.getNodeNotNull(nodeProcess.getParentToken().getNodeId());
                    multiSubprocessFlagsMap.put(process, node instanceof MultiSubprocessNode);
                    if (node instanceof SubprocessNode) {
                        SubprocessNode subprocessNode = (SubprocessNode) node;
                        boolean baseProcessIdMode = subprocessNode.isInBaseProcessIdMode();
                        baseProcessIdModesMap.put(process, baseProcessIdMode);
                        for (VariableMapping variableMapping : subprocessNode.getVariableMappings()) {
                            if (variableMapping.isSyncable() || variableMapping.isReadable()) {
                                readVariableNames.put(variableMapping.getMappedName(), variableMapping.getName());
                            }
                            if (variableMapping.isSyncable()) {
                                syncVariableNames.put(variableMapping.getMappedName(), variableMapping.getName());
                            }
                        }
                        log.debug("Caching for " + process.getId() + " [baseProcessId mode = " + baseProcessIdMode + "]: readVariableNames = "
                                + readVariableNames + "syncVariableNames = " + syncVariableNames);
                    }
                    readVariableNamesMap.put(process, readVariableNames);
                    syncVariableNamesMap.put(process, syncVariableNames);
                }
                log.debug("Caching " + nodeProcess + " for " + process);
                subprocessesInfoMap.put(process, nodeProcess);
            }
            return subprocessesInfoMap.get(process);
        }

        private String getBaseProcessReadVariableName(ProcessDefinition processDefinition, Process process, String name) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                Map<String, String> readVariableNames = readVariableNamesMap.get(process);
                if (!readVariableNames.isEmpty()) {
                    String readVariableName = name;
                    String readVariableNameRemainder = "";
                    while (!readVariableNames.containsKey(readVariableName)) {
                        if (readVariableName.contains(UserType.DELIM)) {
                            int lastIndex = readVariableName.lastIndexOf(UserType.DELIM);
                            readVariableNameRemainder = readVariableName.substring(lastIndex) + readVariableNameRemainder;
                            readVariableName = readVariableName.substring(0, lastIndex);
                        } else if (readVariableName.contains(VariableFormatContainer.COMPONENT_QUALIFIER_START)) {
                            int lastIndex = readVariableName.lastIndexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                            readVariableNameRemainder = readVariableName.substring(lastIndex) + readVariableNameRemainder;
                            readVariableName = readVariableName.substring(0, lastIndex);
                        } else {
                            break;
                        }
                    }
                    if (readVariableNames.containsKey(readVariableName)) {
                        String parentProcessVariableName = readVariableNames.get(readVariableName);
                        if (multiSubprocessFlagsMap.get(process)) {
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_START;
                            parentProcessVariableName += nodeProcess.getIndex();
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        }
                        parentProcessVariableName += readVariableNameRemainder;
                        return parentProcessVariableName;
                    }
                }
            }
            return SystemProperties.isBaseProcessIdModeReadAllVariables() ? name : null;
        }

        private Token getParentProcessToken(Process process) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                return nodeProcess.getParentToken();
            }
            return null;
        }

        private boolean isInBaseProcessIdMode(Process process) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                return baseProcessIdModesMap.get(process);
            }
            return false;
        }

        private VariableDefinition getParentProcessSyncVariableDefinition(ProcessDefinition processDefinition, Process process,
                VariableDefinition variableDefinition) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                Map<String, String> syncVariableNames = syncVariableNamesMap.get(process);
                if (!syncVariableNames.isEmpty()) {
                    String syncVariableName = variableDefinition.getName();
                    String syncVariableNameRemainder = "";
                    while (!syncVariableNames.containsKey(syncVariableName)) {
                        if (syncVariableName.contains(UserType.DELIM)) {
                            int lastIndex = syncVariableName.lastIndexOf(UserType.DELIM);
                            syncVariableNameRemainder = syncVariableName.substring(lastIndex) + syncVariableNameRemainder;
                            syncVariableName = syncVariableName.substring(0, lastIndex);
                        } else if (syncVariableName.contains(VariableFormatContainer.COMPONENT_QUALIFIER_START)) {
                            int lastIndex = syncVariableName.lastIndexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                            syncVariableNameRemainder = syncVariableName.substring(lastIndex) + syncVariableNameRemainder;
                            syncVariableName = syncVariableName.substring(0, lastIndex);
                        } else {
                            break;
                        }
                    }
                    if (syncVariableNames.containsKey(syncVariableName)) {
                        String parentProcessVariableName = syncVariableNames.get(syncVariableName);
                        if (multiSubprocessFlagsMap.get(process)) {
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_START;
                            parentProcessVariableName += nodeProcess.getIndex();
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        }
                        parentProcessVariableName += syncVariableNameRemainder;
                        ProcessDefinition parentProcessDefinition = processDefinitionLoader.getDefinition(nodeProcess.getProcess());
                        return parentProcessDefinition.getVariable(parentProcessVariableName, false);
                    }
                }
            }
            return null;
        }
    }
}
