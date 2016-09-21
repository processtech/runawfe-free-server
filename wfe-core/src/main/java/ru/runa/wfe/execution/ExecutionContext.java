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
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
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
import ru.runa.wfe.var.dao.VariableDAO;
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

    protected ExecutionContext(ApplicationContext applicationContext, ProcessDefinition processDefinition, Token token) {
        this.processDefinition = processDefinition;
        this.token = token;
        Preconditions.checkNotNull(token, "token");
        applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    public ExecutionContext(ProcessDefinition processDefinition, Token token) {
        this(ApplicationContextFactory.getContext(), processDefinition, token);
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

    public Task getTask() {
        return task;
    }

    public NodeProcess getParentNodeProcess() {
        return nodeProcessDAO.findBySubProcessId(getProcess().getId());
    }

    public NodeProcess getChildNodeProcess() {
        return nodeProcessDAO.findByParentToken(getToken());
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
                Swimlane swimlane;
                if (SystemProperties.isSwimlaneAutoInitializationEnabled()) {
                    swimlane = swimlaneDAO.findOrCreateInitialized(this, swimlaneDefinition, false);
                } else {
                    swimlane = swimlaneDAO.findByProcessAndName(getProcess(), swimlaneDefinition.getName());
                }
                return new WfVariable(swimlaneDefinition.toVariableDefinition(), swimlane != null ? swimlane.getExecutor() : null);
            }
        }
        WfVariable variable = variableDAO.getVariable(getProcessDefinition(), getProcess(), name);
        if (variable == null || Utils.isNullOrEmpty(variable.getValue()) || variable.getValue() instanceof UserTypeMap) {
            variable = getVariableUsingBaseProcess(getProcessDefinition(), getProcess(), name, variable);
        }
        if (variable != null) {
            return variable;
        }
        if (name.endsWith(ListFormat.SIZE_SUFFIX) || SystemProperties.isV3CompatibilityMode() || SystemProperties.isAllowedNotDefinedVariables()) {
            Variable<?> dbVariable = variableDAO.get(getProcess(), name);
            return new WfVariable(name, dbVariable != null ? dbVariable.getValue() : null);
        }
        log.debug("No variable defined by '" + name + "' in " + getProcess() + ", returning null");
        return null;
    }

    private WfVariable getVariableUsingBaseProcess(ProcessDefinition processDefinition, Process process, String name, WfVariable variable) {
        String baseProcessIdVariableName = SystemProperties.getBaseProcessIdVariableName();
        if (baseProcessIdVariableName != null && processDefinition.getVariable(baseProcessIdVariableName, false) != null) {
            WfVariable baseProcessIdVariable = variableDAO.getVariable(processDefinition, process, baseProcessIdVariableName);
            if (baseProcessIdVariable != null && baseProcessIdVariable.getValue() != null) {
                String baseProcessIdMappingVariablePrefix = SystemProperties.getBaseProcessIdMappingVariablePrefix();
                if (baseProcessIdMappingVariablePrefix != null) {
                    String baseMappingVariableName = name;
                    String userTypeAttributeName = "";
                    int userTypeAttributeNameStartIndex = name.indexOf(UserType.DELIM);
                    if (userTypeAttributeNameStartIndex != -1) {
                        userTypeAttributeName = name.substring(userTypeAttributeNameStartIndex);
                        baseMappingVariableName = name.substring(0, userTypeAttributeNameStartIndex);
                    }
                    String baseProcessIdMappingVariableName = baseProcessIdMappingVariablePrefix + " " + baseMappingVariableName;
                    WfVariable baseProcessIdMappingVariable = variableDAO.getVariable(processDefinition, process, baseProcessIdMappingVariableName);
                    if (baseProcessIdMappingVariable != null && baseProcessIdMappingVariable.getValue() != null) {
                        log.debug("Mapping rule '" + baseMappingVariableName + "' -> '" + baseProcessIdMappingVariable.getValue() + "'");
                        name = (String) baseProcessIdMappingVariable.getValue() + userTypeAttributeName;
                    }
                }
                log.debug("Loading variable '" + name + "' from process '" + baseProcessIdVariable.getValue() + "'");
                Process baseProcess = processDAO.getNotNull((Long) baseProcessIdVariable.getValue());
                ProcessDefinition baseProcessDefinition = processDefinitionLoader.getDefinition(baseProcess);
                WfVariable baseVariable = variableDAO.getVariable(baseProcessDefinition, baseProcess, name);
                if (variable != null && variable.getValue() instanceof UserTypeMap && baseVariable != null
                        && baseVariable.getValue() instanceof UserTypeMap) {
                    ((UserTypeMap) variable.getValue()).merge((UserTypeMap) baseVariable.getValue(), false);
                } else if (baseVariable != null && !Utils.isNullOrEmpty(baseVariable.getValue())) {
                    return baseVariable;
                }
                return getVariableUsingBaseProcess(baseProcessDefinition, baseProcess, name, variable);
            }
        }
        return variable;
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

    public void setVariableValue(VariableDefinition variableDefinition, Object value) {
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
            VariableDefinition sizeDefinition = new VariableDefinition(sizeVariableName, null, LongFormat.class.getName(), null);
            sizeDefinition.setDefaultValue(0);
            int oldSize = (Integer) variableDAO.getVariableValue(getProcessDefinition(), getProcess(), sizeDefinition);
            int maxSize = Math.max(oldSize, newSize);
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
            setSimpleVariableValue(sizeDefinition, value != null ? newSize : null);
            if (SystemProperties.isV4ListVariableCompatibilityMode()) {
                // delete old list variables as blobs (pre 4.3.0)
                Variable<?> variable = variableDAO.get(getProcess(), variableDefinition.getName());
                if (variable != null) {
                    log.debug("Removing old-style list variable '" + variableDefinition.getName() + "'");
                    variableDAO.delete(variable);
                }
            }
            return;
        }
        setSimpleVariableValue(variableDefinition, value);
    }

    private void setSimpleVariableValue(VariableDefinition variableDefinition, Object value) {
        Variable<?> variable = variableDAO.get(getProcess(), variableDefinition.getName());
        // if there is exist variable and it doesn't support the current type
        if (variable != null && !variable.supports(value)) {
            log.debug("Variable type is changing: deleting old variable '" + variableDefinition.getName() + "' from '" + this + "'");
            variableDAO.delete(variable);
            addLog(new VariableDeleteLog(variable));
            variable = null;
        }
        if (variable == null) {
            if (value != null) {
                variable = variableCreator.create(this, variableDefinition.getName(), value, variableDefinition.getFormatNotNull());
                variableDAO.create(variable);
            }
        } else {
            if (Objects.equal(value, variable.getValue())) {
                // order is valuable due to Timestamp.equals implementation
                return;
            }
            if (ApplicationContextFactory.getDBType() == DBType.ORACLE && Utils.isNullOrEmpty(value) && Utils.isNullOrEmpty(variable.getValue())) {
                return;
            }
            log.debug("Updating variable '" + variableDefinition.getName() + "' in '" + getProcess() + "' to '" + value + "'"
                    + (value != null ? " of " + value.getClass() : ""));
            variable.setValue(this, value, variableDefinition.getFormatNotNull());
        }
        if (value instanceof Date) {
            updateRelatedObjectsDueToDateVariableChange(variableDefinition.getName());
        }
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

    /**
     * Adds all the given variables. It doesn't remove any existing variables unless they are overwritten by the given variables.
     */
    public void setVariableValues(Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setVariableValue(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return variable provider for this process.
     */
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

}
