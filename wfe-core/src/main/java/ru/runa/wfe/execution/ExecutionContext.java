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
import ru.runa.wfe.execution.dao.SwimlaneDAO;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.VariableDAO;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dao.VariableLoaderDAOFallback;
import ru.runa.wfe.var.dto.WfVariable;
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

    private final VariableLoader variableLoader;
    /**
     * This component is used for loading variables with subprocess variables state support.
     */
    private final BaseProcessVariableLoader baseProcessVariableLoader;

    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private VariableCreator variableCreator;
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

    protected ExecutionContext(ApplicationContext applicationContext, ProcessDefinition processDefinition, Token token,
            Map<Process, Map<String, Variable<?>>> loadedVariables) {
        this.processDefinition = processDefinition;
        this.token = token;
        Preconditions.checkNotNull(token, "token");
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
        this.variableLoader = new VariableLoaderDAOFallback(variableDAO, loadedVariables);
        baseProcessVariableLoader = new BaseProcessVariableLoader(variableLoader, getProcessDefinition(), getProcess());
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
        return nodeProcessDAO.getNodeProcessByChild(getProcess().getId());
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
        return baseProcessVariableLoader.get(name);
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
            throw new InternalApplicationException("Variable '" + name + "' is not defined in process definition");
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

    private void setVariableValue(VariableDefinition variableDefinition, Object value) {
        Preconditions.checkNotNull(variableDefinition, "variableDefinition");
        value = convertValueForVariableType(variableDefinition, value);
        ConvertToSimpleVariablesContext context = new ConvertToSimpleVariablesOnSaveContext(variableDefinition, value, getProcess(),
                baseProcessVariableLoader, variableDAO);
        for (ConvertToSimpleVariablesResult simpleVariables : variableDefinition.getFormatNotNull()
                .processBy(new ConvertToSimpleVariables(), context)) {
            setSimpleVariableValue(getProcessDefinition(), getToken(), simpleVariables.variableDefinition, simpleVariables.value);
        }
    }

    private Object convertValueForVariableType(final VariableDefinition variableDefinition, final Object value) {
        if (SystemProperties.isV3CompatibilityMode() || value == null) {
            return value;
        }
        if (!SystemProperties.isStrongVariableFormatEnabled()) {
            return value;
        }
        Class<?> definedClass = variableDefinition.getFormatNotNull().getJavaClass();
        if (!definedClass.isAssignableFrom(value.getClass())) {
            if (SystemProperties.isVariableAutoCastingEnabled()) {
                try {
                    return TypeConversionUtil.convertTo(definedClass, value);
                } catch (Exception e) {
                    throw new InternalApplicationException("Variable '" + variableDefinition.getName() + "' defined as '" + definedClass
                            + "' but value is instance of '" + value.getClass() + "' can't be converted", e);
                }
            } else {
                throw new InternalApplicationException("Variable '" + variableDefinition.getName() + "' defined as '" + definedClass
                        + "' but value is instance of '" + value.getClass() + "'");
            }
        }
        return value;
    }

    private VariableLog setSimpleVariableValue(ProcessDefinition processDefinition, Token token, VariableDefinition variableDefinition, Object value) {
        VariableLog resultingVariableLog = null;
        Variable<?> variable = variableLoader.get(token.getProcess(), variableDefinition.getName());
        // if there is exist variable and it doesn't support the current type
        if (variable != null && !variable.supports(value)) {
            String converterStr = variable.getConverter() == null ? "" : " converter is " + variable.getConverter();
            log.debug("Variable type is changing: deleting old variable '" + variableDefinition.getName() + "' in " + token.getProcess()
                    + " variable value is " + value + converterStr);
            variableDAO.delete(variable);
            resultingVariableLog = new VariableDeleteLog(variable);
            variable = null;
        }
        final BaseProcessVariableLoader.SubprocessSyncCache subprocessSyncCache = baseProcessVariableLoader.getSubprocessSyncCache();
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
            if (value != null && null != token && null != token.getProcess()) {
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
}
