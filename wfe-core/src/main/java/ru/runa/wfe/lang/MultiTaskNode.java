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
package ru.runa.wfe.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.utils.MultiNodeParameters;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code> specifies how task completion triggers continuation of execution.
 */
public class MultiTaskNode extends BaseTaskNode {
    private static final long serialVersionUID = 1L;
    private MultiTaskCreationMode creationMode;
    private String discriminatorUsage;
    private String discriminatorVariableName;
    private String discriminatorCondition;
    private MultiTaskSynchronizationMode synchronizationMode;
    private final List<VariableMapping> variableMappings = Lists.newArrayList();

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(creationMode, "creationMode in " + this);
        Preconditions.checkNotNull(discriminatorVariableName, "discriminatorVariableName in " + this);
        Preconditions.checkNotNull(synchronizationMode, "synchronizationMode in " + this);
    }

    public MultiTaskCreationMode getCreationMode() {
        return creationMode;
    }

    public void setCreationMode(MultiTaskCreationMode creationMode) {
        this.creationMode = creationMode;
    }

    public String getDiscriminatorUsage() {
        return discriminatorUsage;
    }

    public void setDiscriminatorUsage(String discriminatorUsage) {
        this.discriminatorUsage = discriminatorUsage;
    }

    public String getDiscriminatorVariableName() {
        return discriminatorVariableName;
    }

    public void setDiscriminatorVariableName(String discriminatorVariableName) {
        this.discriminatorVariableName = discriminatorVariableName;
    }

    public String getDiscriminatorCondition() {
        return discriminatorCondition;
    }

    public void setDiscriminatorCondition(String discriminatorCondition) {
        this.discriminatorCondition = discriminatorCondition;
    }

    public MultiTaskSynchronizationMode getSynchronizationMode() {
        return synchronizationMode;
    }

    public void setSynchronizationMode(MultiTaskSynchronizationMode executionMode) {
        this.synchronizationMode = executionMode;
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variableMappings) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variableMappings);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MULTI_TASK_STATE;
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        TaskDefinition taskDefinition = getFirstTaskNotNull();
        MultiNodeParameters parameters = new MultiNodeParameters(executionContext, this);
        List<?> data = (List<?>) parameters.getDiscriminatorValue();
        boolean tasksCreated = createTasks(executionContext, taskDefinition, data);
        if (!tasksCreated) {
            log.debug("no tasks were created in " + this);
        }
        // check if we should continue execution
        if (async || !tasksCreated) {
            log.debug("continue execution " + this);
            leave(executionContext);
        }
    }

    private boolean createTasks(ExecutionContext executionContext, TaskDefinition taskDefinition, List<?> data) {
        if (creationMode == MultiTaskCreationMode.BY_EXECUTORS) {
            return createTasksByExecutors(executionContext, taskDefinition, data);
        } else {
            return createTasksByDiscriminator(executionContext, taskDefinition, data);
        }
    }

    private boolean createTasksByExecutors(ExecutionContext executionContext, TaskDefinition taskDefinition, List<?> data) {
        int tasksCounter = 0;
        for (Object executorIdentity : new HashSet<Object>(data)) {
            Executor executor = TypeConversionUtil.convertTo(Executor.class, executorIdentity);
            if (executor == null) {
                log.debug("Executor is null for identity " + executorIdentity);
                continue;
            }
            taskFactory.create(executionContext, taskDefinition, null, executor, tasksCounter);
            tasksCounter++;
        }
        return tasksCounter > 0;
    }

    private boolean createTasksByDiscriminator(ExecutionContext executionContext, TaskDefinition taskDefinition, List<?> data) {
        Swimlane swimlane = executionContext.getProcess().getInitializedSwimlaneNotNull(executionContext, taskDefinition);
        String script = discriminatorCondition;
        if (Utils.isNullOrEmpty(script)) {
            // TODO temporary
            script = (String) executionContext.getVariableValue("multitask condition");
        }
        List<Integer> ignoredIndexes = Lists.newArrayList();
        if (!Utils.isNullOrEmpty(script)) {
            GroovyScriptExecutor scriptExecutor = new GroovyScriptExecutor();
            MapVariableProvider variableProvider = new MapVariableProvider(new HashMap<String, Object>());
            for (int index = 0; index < data.size(); index++) {
                variableProvider.add("item", data.get(index));
                variableProvider.add("index", index);
                boolean result = (Boolean) scriptExecutor.evaluateScript(variableProvider, script);
                if (!result) {
                    ignoredIndexes.add(index);
                }
            }
            log.info("Ignored indexes: " + ignoredIndexes);
        }
        int tasksCounter = 0;
        for (int index = 0; index < data.size(); index++) {
            if (ignoredIndexes.contains(index)) {
                continue;
            }
            taskFactory.create(executionContext, taskDefinition, swimlane, swimlane.getExecutor(), index);
            tasksCounter++;
        }
        return tasksCounter > 0;
    }

    public boolean isCompletionTriggersSignal(Task task) {
        switch (synchronizationMode) {
        case FIRST:
            return true;
        case LAST:
            return isLastTaskToComplete(task);
        default:
            return false;
        }
    }

    private boolean isLastTaskToComplete(Task task) {
        Token token = task.getToken();
        boolean lastToComplete = true;
        for (Task other : token.getTasks()) {
            if (!other.equals(task)) {
                lastToComplete = false;
                break;
            }
        }
        return lastToComplete;
    }
}
