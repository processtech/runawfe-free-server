package ru.runa.wfe.lang;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.Variables;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class SubprocessNode extends VariableContainerNode implements Synchronizable {
    private static final long serialVersionUID = 1L;

    private String subProcessName;
    private boolean embedded;
    @Autowired
    private transient IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private transient ProcessFactory processFactory;

    protected boolean async;
    protected AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.NEVER;

    @Override
    public NodeType getNodeType() {
        return NodeType.SUBPROCESS;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(subProcessName, "subProcessName in " + this);
        if (isEmbedded()) {
            if (getLeavingTransitions().size() != 1) {
                throw new InternalApplicationException("Subprocess state for embedded subprocess should have 1 leaving transition");
            }
        }
    }

    public String getSubProcessName() {
        return subProcessName;
    }

    public void setSubProcessName(String subProcessName) {
        this.subProcessName = subProcessName;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        this.async = async;
    }

    @Override
    public AsyncCompletionMode getCompletionMode() {
        return asyncCompletionMode;
    }

    @Override
    public void setCompletionMode(AsyncCompletionMode completionMode) {
        this.asyncCompletionMode = completionMode;
    }

    protected ProcessDefinition getSubProcessDefinition() {
        return processDefinitionLoader.getLatestDefinition(subProcessName);
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        if (isEmbedded()) {
            throw new InternalApplicationException("it's not intended for execution");
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put(Variables.CURRENT_PROCESS_ID_WRAPPED, executionContext.getProcess().getId());
        map.put(Variables.CURRENT_PROCESS_DEFINITION_NAME_WRAPPED, executionContext.getProcessDefinition().getName());
        map.put(Variables.CURRENT_NODE_NAME_WRAPPED, executionContext.getNode().getName());
        map.put(Variables.CURRENT_NODE_ID_WRAPPED, executionContext.getNode().getNodeId());
        IVariableProvider variableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
        Map<String, Object> variables = Maps.newHashMap();
        if (isInBaseIdProcessMode()) {
            Long baseProcessId = variableProvider.getValueNotNull(Long.class, getBaseIdProcessVariableName());
            log.debug("executing in base_process_id: " + baseProcessId);
            variables.put(SystemProperties.getBaseProcessIdVariableName(), baseProcessId);
        } else {
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable mapping is readable
                if (variableMapping.isReadable() || variableMapping.isSyncable()) {
                    // the variable is copied from the super process variable
                    // name to the sub process mapped name
                    String variableName = variableMapping.getName();
                    Object value = variableProvider.getValue(variableName);
                    if (value != null) {
                        String mappedName = variableMapping.getMappedName();
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value
                                + (value != null ? " of " + value.getClass() : ""));
                        variables.put(mappedName, value);
                    }
                }
            }
        }
        ProcessDefinition subProcessDefinition = getSubProcessDefinition();
        Process subProcess = processFactory.createSubprocess(executionContext, subProcessDefinition, variables, 0);
        processFactory.startSubprocess(executionContext, new ExecutionContext(subProcessDefinition, subProcess));
        if (async) {
            log.debug("continue execution in async " + this);
            leave(executionContext);
        }
    }

    @Override
    public void leave(ExecutionContext subExecutionContext, Transition transition) {
        if (async) {
            super.leave(subExecutionContext, transition);
            return;
        }
        if (getClass() == SubprocessNode.class) {
            Process subProcess = subExecutionContext.getProcess();
            ExecutionContext executionContext = getParentExecutionContext(subExecutionContext);
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is writable
                if (variableMapping.isWritable()) {
                    // the variable is copied from the sub process mapped name
                    // to the super process variable name
                    String mappedName = variableMapping.getMappedName();
                    Object value = subExecutionContext.getVariableProvider().getValue(mappedName);
                    if (value != null) {
                        String variableName = variableMapping.getName();
                        log.debug("copying sub process var '" + mappedName + "' to super process var '" + variableName + "': " + value);
                        executionContext.setVariableValue(variableName, value);
                    }
                }
            }
            executionContext.addLog(new SubprocessEndLog(this, executionContext.getToken(), subProcess));
            super.leave(executionContext, transition);
        } else {
            super.leave(subExecutionContext, transition);
        }
    }

    protected ExecutionContext getParentExecutionContext(ExecutionContext subExecutionContext) {
        NodeProcess parentNodeProcess = subExecutionContext.getParentNodeProcess();
        Long superDefinitionId = parentNodeProcess.getProcess().getDeployment().getId();
        ProcessDefinition superDefinition = processDefinitionLoader.getDefinition(superDefinitionId);
        return new ExecutionContext(superDefinition, parentNodeProcess.getParentToken());
    }

}
