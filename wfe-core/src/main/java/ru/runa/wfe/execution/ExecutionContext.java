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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.SwimlaneDao;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableCreator;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dao.BaseProcessVariableLoader;
import ru.runa.wfe.var.dao.VariableDao;
import ru.runa.wfe.var.dao.VariableLoader;
import ru.runa.wfe.var.dao.VariableLoaderDAOFallback;
import ru.runa.wfe.var.dao.VariableLoaderFromMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;

@CommonsLog
public class ExecutionContext {
    private final ParsedProcessDefinition parsedProcessDefinition;
    private final Token token;
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
    private TokenDao tokenDAO;
    @Autowired
    private NodeProcessDao nodeProcessDAO;
    @Autowired
    private ProcessLogDao processLogDAO;
    @Autowired
    private VariableDao variableDAO;
    @Autowired
    private TaskDao taskDAO;
    @Autowired
    private JobDao jobDAO;
    @Autowired
    private SwimlaneDao swimlaneDAO;

    protected ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token,
            Map<Process, Map<String, Variable<?>>> loadedVariables, boolean disableVariableDaoLoading) {
        this.parsedProcessDefinition = parsedProcessDefinition;
        this.token = token;
        Preconditions.checkNotNull(token, "token");
        ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(this);
        if (disableVariableDaoLoading) {
            this.variableLoader = new VariableLoaderFromMap(loadedVariables);
        } else {
            this.variableLoader = new VariableLoaderDAOFallback(variableDAO, loadedVariables);
        }
        this.baseProcessVariableLoader = new BaseProcessVariableLoader(variableLoader, getParsedProcessDefinition(), getProcess());
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token, Map<Process, Map<String, Variable<?>>> loadedVariables) {
        this(parsedProcessDefinition, token, loadedVariables, false);
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process, Map<Process, Map<String, Variable<?>>> loadedVariables,
            boolean disableVariableDaoLoading) {
        this(parsedProcessDefinition, process.getRootToken(), loadedVariables, disableVariableDaoLoading);
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Token token) {
        this(parsedProcessDefinition, token, null, false);
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Process process) {
        this(parsedProcessDefinition, process.getRootToken(), null, false);
    }

    public ExecutionContext(ParsedProcessDefinition parsedProcessDefinition, Task task) {
        this(parsedProcessDefinition, task.getToken(), null, false);
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
        return getToken().getNodeNotNull(getParsedProcessDefinition());
    }

    public ParsedProcessDefinition getParsedProcessDefinition() {
        return parsedProcessDefinition;
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
        List<Task> tasks = taskDAO.findByProcessAndNodeId(token.getProcess(), token.getNodeId());
        return tasks.isEmpty() ? null : tasks.get(0);
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
            SwimlaneDefinition swimlaneDefinition = getParsedProcessDefinition().getSwimlane(name);
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
        SwimlaneDefinition swimlaneDefinition = getParsedProcessDefinition().getSwimlane(name);
        if (swimlaneDefinition != null) {
            log.debug("Assigning swimlane '" + name + "' value '" + value + "'");
            Swimlane swimlane = swimlaneDAO.findOrCreate(getProcess(), swimlaneDefinition);
            swimlane.assignExecutor(this, (Executor) convertValueForVariableType(swimlaneDefinition.toVariableDefinition(), value), true);
            return;
        }
        VariableDefinition variableDefinition = getParsedProcessDefinition().getVariable(name, false);
        if (variableDefinition == null) {
            if (value == null) {
                return;
            }
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

    public void activateTokenIfHasPreviousError() {
        if (getToken().getExecutionStatus() == ExecutionStatus.FAILED) {
            getToken().setExecutionStatus(ExecutionStatus.ACTIVE);
            getToken().setErrorDate(null);
            getToken().setErrorMessage(null);
            List<Token> failedTokens = tokenDAO.findByProcessAndExecutionStatus(getProcess(), ExecutionStatus.FAILED);
            if (failedTokens.isEmpty()) {
                getProcess().setExecutionStatus(ExecutionStatus.ACTIVE);
            }
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("processId", getToken().getProcess().getId()).add("tokenId", getToken().getId()).toString();
    }

    private void setVariableValue(VariableDefinition variableDefinition, Object value) {
        Preconditions.checkNotNull(variableDefinition, "variableDefinition");
        switch (variableDefinition.getStoreType()) {
        case BLOB: {
            setSimpleVariableValue(getParsedProcessDefinition(), getToken(), variableDefinition, value);
            break;
        }
        case TRANSIENT: {
            setTransientVariable(variableDefinition.getName(), value);
            break;
        }
        case DEFAULT: {
            ConvertToSimpleVariablesContext context;
            context = new ConvertToSimpleVariablesOnSaveContext(variableDefinition, value, getProcess(), baseProcessVariableLoader, variableDAO);
            VariableFormat variableFormat = variableDefinition.getFormatNotNull();
            for (ConvertToSimpleVariablesResult simpleVariables : variableFormat.processBy(new ConvertToSimpleVariables(), context)) {
                Object convertedValue = convertValueForVariableType(simpleVariables.variableDefinition, simpleVariables.value);
                setSimpleVariableValue(getParsedProcessDefinition(), getToken(), simpleVariables.variableDefinition, convertedValue);
            }
            break;
        }
        default: {
            throw new InternalApplicationException("Unexpected " + variableDefinition.getStoreType());
        }
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
                    if (Executor.class.isAssignableFrom(definedClass) && value instanceof List) {
                        Group tmpGroup = TemporaryGroup.create(getProcess().getId(), variableDefinition.getName());
                        List<Executor> executors = Lists.newArrayList();
                        for (Object executorIdentity : (List) value) {
                            executors.add(TypeConversionUtil.convertTo(Executor.class, executorIdentity));
                        }
                        tmpGroup = ApplicationContextFactory.getExecutorLogic().saveTemporaryGroup(tmpGroup, executors);
                        return tmpGroup;
                    }
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

    private VariableLog setSimpleVariableValue(ParsedProcessDefinition parsedProcessDefinition, Token token, VariableDefinition variableDefinition, Object value) {
        VariableLog resultingVariableLog = null;
        Variable<?> variable = variableLoader.get(token.getProcess(), variableDefinition.getName());
        // if there is exist variable and it doesn't support the current type
        if (variable != null && !variable.supports(value)) {
            String converterStr = variable.getConverter() == null ? "" : " converter is " + variable.getConverter();
            log.debug("Variable type is changing: deleting old variable '" + variableDefinition.getName() + "' in " + token.getProcess()
                    + " variable value is " + value + converterStr);
            variableDAO.delete(variable);
            variableDAO.flushPendingChanges();
            resultingVariableLog = new VariableDeleteLog(variable);
            variable = null;
        }
        final BaseProcessVariableLoader.SubprocessSyncCache subprocessSyncCache = baseProcessVariableLoader.getSubprocessSyncCache();
        if (variable == null) {
            VariableDefinition syncVariableDefinition = subprocessSyncCache.getParentProcessSyncVariableDefinition(parsedProcessDefinition,
                    token.getProcess(), variableDefinition);
            if (syncVariableDefinition != null) {
                Token parentToken = subprocessSyncCache.getParentProcessToken(token.getProcess());
                ParsedProcessDefinition parsedParentProcessDefinition = processDefinitionLoader.getDefinition(parentToken.getProcess());
                log.debug("Setting " + token.getProcess().getId() + "." + variableDefinition.getName() + " in parent process "
                        + parentToken.getProcess().getId() + "." + syncVariableDefinition.getName());
                VariableLog parentVariableLog = setSimpleVariableValue(parsedParentProcessDefinition, parentToken, syncVariableDefinition, value);
                if (parentVariableLog != null) {
                    VariableLog markingVariableLog = parentVariableLog.getContentCopy();
                    markingVariableLog.setVariableName(variableDefinition.getName());
                    resultingVariableLog = markingVariableLog;
                }
            }
            if (value != null) {
                if (syncVariableDefinition == null || !subprocessSyncCache.isInBaseProcessIdMode(token.getProcess())) {
                    variable = variableCreator.create(token.getProcess(), variableDefinition, value);
                    resultingVariableLog = variable.setValue(this, value, variableDefinition);
                    variableDAO.create(variable);
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
            resultingVariableLog = variable.setValue(this, value, variableDefinition);
            VariableDefinition syncVariableDefinition = subprocessSyncCache.getParentProcessSyncVariableDefinition(parsedProcessDefinition,
                    token.getProcess(), variableDefinition);
            if (syncVariableDefinition != null) {
                Token parentToken = subprocessSyncCache.getParentProcessToken(token.getProcess());
                ParsedProcessDefinition parsedParentProcessDefinition = processDefinitionLoader.getDefinition(parentToken.getProcess());
                log.debug("Setting " + token.getProcess().getId() + "." + variableDefinition.getName() + " in parent process "
                        + parentToken.getProcess().getId() + "." + syncVariableDefinition.getName());
                setSimpleVariableValue(parsedParentProcessDefinition, parentToken, syncVariableDefinition, value);
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
