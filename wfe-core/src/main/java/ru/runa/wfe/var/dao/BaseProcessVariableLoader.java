package ru.runa.wfe.var.dao;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.NodeProcessDao;
import ru.runa.wfe.execution.dao.ProcessDao;
import ru.runa.wfe.lang.MultiSubprocessNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.var.BaseVariable;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

@CommonsLog
public class BaseProcessVariableLoader {
    private final VariableLoader variableLoader;
    private final Process process;
    private final ProcessDefinition processDefinition;
    private final SubprocessSyncCache subprocessSyncCache;

    @Autowired
    private NodeProcessDao nodeProcessDao;
    @Autowired
    private ProcessDao processDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    public BaseProcessVariableLoader(VariableLoader variableLoader, ProcessDefinition processDefinition, Process process) {
        this.variableLoader = variableLoader;
        this.process = process;
        this.processDefinition = processDefinition;
        ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(this);
        this.subprocessSyncCache = new SubprocessSyncCache(this);
    }

    public WfVariable get(String name) {
        WfVariable variable = variableLoader.getVariable(processDefinition, process, name);
        if (variable != null) {
            if (Utils.isNullOrEmpty(variable.getValue()) || Objects.equal(variable.getDefinition().getDefaultValue(), variable.getValue())
                    || variable.getValue() instanceof UserTypeMap) {
                variable = getVariableUsingBaseProcess(processDefinition, process, name, variable);
            }
            return variable;
        }
        if (SystemProperties.isV3CompatibilityMode()) {
            BaseVariable dbVariable = variableLoader.get(process, name);
            return new WfVariable(name, dbVariable != null ? dbVariable.getValue() : null);
        }
        log.debug("No variable defined by '" + name + "' in " + process + ", returning null");
        return null;
    }

    public SubprocessSyncCache getSubprocessSyncCache() {
        return this.subprocessSyncCache;
    }

    private WfVariable getVariableUsingBaseProcess(ProcessDefinition processDefinition, Process process, String name, WfVariable variable) {
        Long baseProcessId = subprocessSyncCache.getBaseProcessId(processDefinition, process);
        if (baseProcessId != null) {
            name = subprocessSyncCache.getBaseProcessReadVariableName(process, name);
            if (name != null) {
                log.debug("Loading variable '" + name + "' from process '" + baseProcessId + "'");
                Process baseProcess = processDao.getNotNull(baseProcessId);
                ProcessDefinition baseProcessDefinition = processDefinitionLoader.getDefinition(baseProcess);
                WfVariable baseVariable = variableLoader.getVariable(baseProcessDefinition, baseProcess, name);
                if (variable != null && variable.getValue() instanceof UserTypeMap && baseVariable != null
                        && baseVariable.getValue() instanceof UserTypeMap) {
                    ((UserTypeMap) variable.getValue()).merge((UserTypeMap) baseVariable.getValue(), false);
                } else if (baseVariable != null) {
                    if (!Utils.isNullOrEmpty(baseVariable.getValue()) || variable.getValue() == null) {
                        variable.setValue(baseVariable.getValue());
                    }
                    if (!Utils.isNullOrEmpty(variable.getValue())
                            && !Objects.equal(baseVariable.getDefinition().getDefaultValue(), variable.getValue())) {
                        return variable;
                    }
                }
                return getVariableUsingBaseProcess(baseProcessDefinition, baseProcess, name, variable);
            }
        }
        return variable;
    }

    public static class SubprocessSyncCache {
        private final Map<Process, Boolean> baseProcessIdModesMap = Maps.newHashMap();
        private final Map<Process, Boolean> multiSubprocessFlagsMap = Maps.newHashMap();
        private final Map<Process, Map<String, String>> readVariableNamesMap = Maps.newHashMap();
        private final Map<Process, Map<String, String>> syncVariableNamesMap = Maps.newHashMap();
        private final Map<Process, Long> baseProcessIdsMap = Maps.newHashMap();
        private Map<Process, NodeProcess> subprocessesInfoMap = Maps.newHashMap();
        private final BaseProcessVariableLoader baseProcessVariableLoader;

        SubprocessSyncCache(BaseProcessVariableLoader baseProcessVariableLoader) {
            this.baseProcessVariableLoader = baseProcessVariableLoader;
        }

        private Long getBaseProcessId(ProcessDefinition processDefinition, Process process) {
            if (!baseProcessIdsMap.containsKey(process)) {
                String baseProcessIdVariableName = SystemProperties.getBaseProcessIdVariableName();
                if (baseProcessIdVariableName != null && processDefinition.getVariable(baseProcessIdVariableName, false) != null) {
                    WfVariable baseProcessIdVariable = baseProcessVariableLoader.variableLoader.getVariable(processDefinition, process,
                            baseProcessIdVariableName);
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
                NodeProcess nodeProcess = baseProcessVariableLoader.nodeProcessDao.findBySubProcess(process);
                if (nodeProcess != null) {
                    Map<String, String> readVariableNames = Maps.newHashMap();
                    Map<String, String> syncVariableNames = Maps.newHashMap();
                    ProcessDefinition parentProcessDefinition = baseProcessVariableLoader.processDefinitionLoader.getDefinition(nodeProcess
                            .getProcess());
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

        private String getBaseProcessReadVariableName(Process process, String name) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                Map<String, String> readVariableNames = readVariableNamesMap.get(process);
                if (!readVariableNames.isEmpty()) {
                    final VariableNameInfo readVariableInfo = VariableNameInfo.createFrom(name, readVariableNames);
                    if (readVariableNames.containsKey(readVariableInfo.getVariableName())) {
                        String parentProcessVariableName = readVariableNames.get(readVariableInfo.getVariableName());
                        if (multiSubprocessFlagsMap.get(process)) {
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_START;
                            parentProcessVariableName += nodeProcess.getIndex();
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        }
                        parentProcessVariableName += readVariableInfo.getVariableNameRemainder();
                        return parentProcessVariableName;
                    }
                }
            }
            return SystemProperties.isBaseProcessIdModeReadAllVariables() ? name : null;
        }

        public Token getParentProcessToken(Process process) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                return nodeProcess.getParentToken();
            }
            return null;
        }

        public boolean isInBaseProcessIdMode(Process process) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                Boolean isInBaseProcessMode = baseProcessIdModesMap.get(process);
                return isInBaseProcessMode == null ? false : isInBaseProcessMode;
            }
            return false;
        }

        public VariableDefinition getParentProcessSyncVariableDefinition(Process process, VariableDefinition variableDefinition) {
            NodeProcess nodeProcess = getSubprocessNodeInfo(process);
            if (nodeProcess != null) {
                Map<String, String> syncVariableNames = syncVariableNamesMap.get(process);
                if (!syncVariableNames.isEmpty()) {
                    final VariableNameInfo syncVariableInfo = VariableNameInfo.createFrom(variableDefinition.getName(), syncVariableNames);
                    if (syncVariableNames.containsKey(syncVariableInfo.getVariableName())) {
                        String parentProcessVariableName = syncVariableNames.get(syncVariableInfo.getVariableName());
                        if (multiSubprocessFlagsMap.get(process)) {
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_START;
                            parentProcessVariableName += nodeProcess.getIndex();
                            parentProcessVariableName += VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        }
                        parentProcessVariableName += syncVariableInfo.getVariableNameRemainder();
                        ProcessDefinition parentProcessDefinition = baseProcessVariableLoader.processDefinitionLoader.getDefinition(
                                nodeProcess.getProcess()
                        );
                        return parentProcessDefinition.getVariable(parentProcessVariableName, false);
                    }
                }
            }
            return null;
        }

        @RequiredArgsConstructor
        private static class VariableNameInfo {
            private final String variableName;
            private final String variableNameRemainder;

            String getVariableName() {
                return variableName;
            }

            String getVariableNameRemainder() {
                return variableNameRemainder;
            }

            static VariableNameInfo createFrom(String name, Map<String, String> variableNames) {
                String variableName = name;
                String variableNameRemainder = "";
                while (!variableNames.containsKey(variableName)) {
                    if (variableName.contains(UserType.DELIM)) {
                        int lastIndex = variableName.lastIndexOf(UserType.DELIM);
                        variableNameRemainder = variableName.substring(lastIndex) + variableNameRemainder;
                        variableName = variableName.substring(0, lastIndex);
                    } else if (variableName.contains(VariableFormatContainer.COMPONENT_QUALIFIER_START)) {
                        int lastIndex = variableName.lastIndexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
                        variableNameRemainder = variableName.substring(lastIndex) + variableNameRemainder;
                        variableName = variableName.substring(0, lastIndex);
                    } else {
                        break;
                    }
                }
                return new VariableNameInfo(variableName, variableNameRemainder);
            }
        }
    }
}
