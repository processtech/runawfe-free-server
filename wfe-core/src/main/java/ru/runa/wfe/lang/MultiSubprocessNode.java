package ru.runa.wfe.lang;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.SubprocessEndLog;
import ru.runa.wfe.commons.GroovyScriptExecutor;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessFactory;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.lang.utils.MultiinstanceUtils;
import ru.runa.wfe.lang.utils.MultiinstanceUtils.Parameters;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.SelectableOption;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.Variables;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;

public class MultiSubprocessNode extends SubprocessNode {
    private static final long serialVersionUID = 1L;

    @Autowired
    private transient ProcessFactory processFactory;
    @Autowired
    private transient NodeProcessDao nodeProcessDao;

    private String discriminatorCondition;

    @Override
    public NodeType getNodeType() {
        return NodeType.MULTI_SUBPROCESS;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        log.debug("Executing " + this + " with " + executionContext);
        Parameters parameters = MultiinstanceUtils.parse(executionContext, this);
        List<Object> data = TypeConversionUtil.convertTo(List.class, parameters.getDiscriminatorValue());
        List<Process> subProcesses = Lists.newArrayList();
        ProcessDefinition subProcessDefinition = getSubProcessDefinition();
        List<Integer> ignoredIndexes = Lists.newArrayList();
        if (!Utils.isNullOrEmpty(discriminatorCondition)) {
            GroovyScriptExecutor scriptExecutor = new GroovyScriptExecutor();
            MapVariableProvider variableProvider = new MapVariableProvider(new HashMap<String, Object>());
            for (int index = 0; index < data.size(); index++) {
                variableProvider.add("item", data.get(index));
                variableProvider.add("index", index);
                boolean result = (Boolean) scriptExecutor.evaluateScript(variableProvider, discriminatorCondition);
                if (!result) {
                    ignoredIndexes.add(index);
                }
            }
            log.debug("Ignored indexes: " + ignoredIndexes);
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put(Variables.CURRENT_PROCESS_ID_WRAPPED, executionContext.getProcess().getId());
        map.put(Variables.CURRENT_PROCESS_DEFINITION_NAME_WRAPPED, executionContext.getProcessDefinition().getName());
        map.put(Variables.CURRENT_NODE_NAME_WRAPPED, executionContext.getNode().getName());
        map.put(Variables.CURRENT_NODE_ID_WRAPPED, executionContext.getNode().getNodeId());
        VariableProvider variableProvider = new MapDelegableVariableProvider(map, executionContext.getVariableProvider());
        for (int index = 0; index < data.size(); index++) {
            if (ignoredIndexes.contains(index)) {
                continue;
            }
            Map<String, Object> variables = Maps.newHashMap();
            Object discriminatorValue = TypeConversionUtil.getListValue(parameters.getDiscriminatorValue(), index);
            if (discriminatorValue instanceof SelectableOption) {
                discriminatorValue = ((SelectableOption) discriminatorValue).getValue();
            }
            log.debug("setting discriminator var '" + parameters.getDiscriminatorVariableName() + "' to sub process var '"
                    + parameters.getIteratorVariableName() + "': " + discriminatorValue);
            variables.put(parameters.getIteratorVariableName(), discriminatorValue);
            boolean baseProcessIdMode = isInBaseProcessIdMode();
            for (VariableMapping variableMapping : variableMappings) {
                String variableName = variableMapping.getName();
                String mappedName = variableMapping.getMappedName();
                boolean isSwimlane = subProcessDefinition.getSwimlane(mappedName) != null;
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
                        log.debug("copying super process var '" + variableName + "' to sub process var '" + mappedName + "': " + value + " of "
                                + value.getClass());
                        if (variableMapping.isMultiinstanceLink()) {
                            variables.put(mappedName, TypeConversionUtil.getListValue(value, index));
                        } else {
                            variables.put(mappedName, value);
                        }
                    } else {
                        log.warn("super process var '" + variableName + "' is null (ignored mapping to '" + mappedName + "')");
                    }
                }
            }
            Process subProcess = processFactory.createSubprocess(executionContext, subProcessDefinition, variables, index, isValidateAtStart());
            subProcesses.add(subProcess);
        }
        for (Process subprocess : subProcesses) {
            ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocess);
            processFactory.startSubprocess(executionContext, subExecutionContext);
        }
        MultiinstanceUtils.autoExtendContainerVariables(executionContext, getVariableMappings(), data.size());
        if (subProcesses.size() == 0) {
            log.debug("Leaving multisubprocess state due to 0 subprocesses");
            super.leave(executionContext, null);
        }
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
        ExecutionContext executionContext = getParentExecutionContext(subExecutionContext);
        NodeProcess nodeProcess = subExecutionContext.getParentNodeProcess();
        if (nodeProcess.getIndex() == null) {
            // pre AddSubProcessIndexColumn mode
            leaveBackCompatiblePre410(executionContext, transition);
        } else {
            if (SystemProperties.isMultiSubprocessDataCompatibilityMode()) {
                MultiinstanceUtils.autoExtendContainerVariables(executionContext, getVariableMappings(), nodeProcess.getIndex() + 1);
            }
            for (VariableMapping variableMapping : variableMappings) {
                // if this variable access is writable
                if (variableMapping.isWritable()) {
                    String subprocessVariableName = variableMapping.getMappedName();
                    String processVariableName = variableMapping.getName();
                    WfVariable variable = executionContext.getVariableProvider().getVariableNotNull(processVariableName);
                    Object value;
                    if (variableMapping.isMultiinstanceLink()) {
                        value = TypeConversionUtil.convertTo(List.class, variable.getValue());
                        if (value == null) {
                            value = Lists.newArrayList();
                        }
                        List<Object> list = (List<Object>) value;
                        while (list.size() <= nodeProcess.getIndex()) {
                            list.add(null);
                        }
                        list.set(nodeProcess.getIndex(), subExecutionContext.getVariableProvider().getValue(subprocessVariableName));
                    } else {
                        value = subExecutionContext.getVariableProvider().getValue(subprocessVariableName);
                    }
                    log.debug("copying sub process var '" + subprocessVariableName + "' to process var '" + processVariableName + "': " + value);
                    executionContext.setVariableValue(processVariableName, value);
                }
            }
            executionContext.addLog(new SubprocessEndLog(this, executionContext.getToken(), nodeProcess.getSubProcess()));
            if (executionContext.getNotEndedSubprocesses().size() == 0) {
                log.debug("Leaving multisubprocess state");
                super.leave(executionContext, transition);
            }
        }
    }

    private void leaveBackCompatiblePre410(ExecutionContext executionContext, Transition transition) {
        if (executionContext.getNotEndedSubprocesses().size() == 0) {
            log.debug("Leaving multisubprocess state [in backcompatibility mode] due to 0 active subprocesses");
            List<Process> subprocesses = nodeProcessDao.getSubprocesses(executionContext.getProcess(), executionContext.getToken().getNodeId(),
                    executionContext.getToken(), null);
            if (!subprocesses.isEmpty()) {
                ProcessDefinition subProcessDefinition = getSubProcessDefinition();
                for (VariableMapping variableMapping : variableMappings) {
                    // if this variable access is writable
                    if (variableMapping.isWritable()) {
                        String subprocessVariableName = variableMapping.getMappedName();
                        String processVariableName = variableMapping.getName();
                        WfVariable variable = executionContext.getVariableProvider().getVariable(processVariableName);
                        Object value;
                        if (variable == null || variable.getDefinition().getFormatNotNull() instanceof ListFormat) {
                            value = new ArrayList<Object>();
                            for (Process subprocess : subprocesses) {
                                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocess);
                                ((List<Object>) value).add(subExecutionContext.getVariableValue(subprocessVariableName));
                            }
                        } else {
                            if (subprocesses.size() > 0) {
                                ExecutionContext subExecutionContext = new ExecutionContext(subProcessDefinition, subprocesses.get(0));
                                value = subExecutionContext.getVariableValue(subprocessVariableName);
                            } else {
                                value = null;
                            }
                        }
                        log.debug("copying sub process var '" + subprocessVariableName + "' to process var '" + processVariableName + "': " + value);
                        executionContext.setVariableValue(processVariableName, value);
                    }
                }
            }
            for (Process subProcess : subprocesses) {
                executionContext.addLog(new SubprocessEndLog(this, executionContext.getToken(), subProcess));
            }
            super.leave(executionContext, transition);
        }
    }

    public String getDiscriminatorCondition() {
        return discriminatorCondition;
    }

    public void setDiscriminatorCondition(String discriminatorCondition) {
        this.discriminatorCondition = discriminatorCondition;
    }

}
