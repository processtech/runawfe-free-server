package ru.runa.wfe.lang;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.DeploymentWithVersion;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.Variables;

public class SubprocessNode extends VariableContainerNode implements Synchronizable, BoundaryEventContainer {
    private static final long serialVersionUID = 1L;

    protected boolean async;
    protected AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.NEVER;
    private String subProcessName;
    private boolean embedded;
    private boolean transactional;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Autowired
    private transient IProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private transient ProcessFactory processFactory;

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

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

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
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

    protected ParsedProcessDefinition getSubProcessDefinition() {
        long version = getParsedProcessDefinition().getProcessDefinitionVersion().getVersion();
        if (version < 0) {
            DeploymentWithVersion dwv = ApplicationContextFactory.getDeploymentDAO().findDeployment(subProcessName, version);
            return processDefinitionLoader.getDefinition(dwv.processDefinitionVersion.getId());
        }
        Date beforeDate = getParsedProcessDefinition().getProcessDefinitionVersion().getSubprocessBindingDate();
        if (beforeDate != null) {
            Long processDefinitionVersionId = ApplicationContextFactory.getDeploymentDAO().findDeploymentVersionIdLatestVersionBeforeDate(subProcessName, beforeDate);
            if (processDefinitionVersionId == null) {
                throw new InternalApplicationException("No definition \"" + subProcessName + "\" found before " + CalendarUtil.formatDateTime(beforeDate));
            }
            return processDefinitionLoader.getDefinition(processDefinitionVersionId);
        }
        return processDefinitionLoader.getLatestDefinition(subProcessName);
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        if (isEmbedded()) {
            throw new InternalApplicationException("it's not intended for execution");
        }
        val map = new HashMap<String, Object>();
        map.put(Variables.CURRENT_PROCESS_ID_WRAPPED, executionContext.getProcess().getId());
        map.put(Variables.CURRENT_PROCESS_DEFINITION_NAME_WRAPPED, executionContext.getParsedProcessDefinition().getName());
        map.put(Variables.CURRENT_NODE_NAME_WRAPPED, executionContext.getNode().getName());
        map.put(Variables.CURRENT_NODE_ID_WRAPPED, executionContext.getNode().getNodeId());
        val variableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
        val variables = new HashMap<String, Object>();
        boolean baseProcessIdMode = isInBaseProcessIdMode();
        ParsedProcessDefinition parsedSubProcessDefinition = getSubProcessDefinition();
        for (VariableMapping variableMapping : variableMappings) {
            String variableName = variableMapping.getName();
            String mappedName = variableMapping.getMappedName();
            boolean isSwimlane = parsedSubProcessDefinition.getSwimlane(mappedName) != null;
            if (isSwimlane && variableMapping.isSyncable()) {
                throw new InternalApplicationException("Sync mode does not supported for swimlane " + mappedName);
            }
            boolean copyValue;
            if (baseProcessIdMode) {
                copyValue = variableMapping.isReadable() && (isSwimlane || SystemProperties.getBaseProcessIdVariableName().equals(mappedName));
            } else {
                copyValue = variableMapping.isReadable() || variableMapping.isSyncable();
            }
            if (copyValue) {
                Object value = variableProvider.getValue(variableName);
                if (value != null) {
                    log.debug("copying " + variableName + " to subprocess variable " + mappedName + ": '" + value + "' of " + value.getClass());
                    variables.put(mappedName, value);
                } else {
                    log.warn(variableName + " is null (ignored mapping to subprocess variable " + mappedName + ")");
                }
            }
        }
        Process subProcess = processFactory.createSubprocess(executionContext, parsedSubProcessDefinition, variables, 0);
        processFactory.startSubprocess(executionContext, new ExecutionContext(parsedSubProcessDefinition, subProcess));
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
        ParsedProcessDefinition superDefinition = processDefinitionLoader.getDefinition(parentNodeProcess.getProcess());
        return new ExecutionContext(superDefinition, parentNodeProcess.getParentToken());
    }

    @Override
    protected boolean endBoundaryEventTokensOnNodeLeave() {
        return !async;
    }

    @Override
    protected void onBoundaryEvent(ParsedProcessDefinition parsedProcessDefinition, Token token, BoundaryEvent boundaryEvent) {
        super.onBoundaryEvent(parsedProcessDefinition, token, boundaryEvent);
        if (async) {
            List<Process> processes = new ExecutionContext(parsedProcessDefinition, token).getTokenSubprocesses();
            for (Process process : processes) {
                if (process.hasEnded()) {
                    continue;
                }
                process.end(new ExecutionContext(getSubProcessDefinition(), process), null);
            }
        }
    }

}
