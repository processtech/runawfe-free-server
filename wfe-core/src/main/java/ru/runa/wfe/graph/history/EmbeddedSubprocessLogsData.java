package ru.runa.wfe.graph.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import ru.runa.wfe.audit.NodeEnterLog;
import ru.runa.wfe.audit.NodeLeaveLog;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.TransitionLog;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.SubprocessNode;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Component to split logs to main process logs and embedded subprocesses logs.
 */
public class EmbeddedSubprocessLogsData {
    /**
     * Logs, belongs to process, except embedded subprocesses logs.
     */
    private final List<ProcessLog> processLogsWithoutEmbedded = Lists.newArrayList();
    /**
     * Maps from embedded subprocess to it's logs.
     */
    private final HashMap<String, ArrayList<ProcessLog>> embeddedSubprocessesLogs = new HashMap<String, ArrayList<ProcessLog>>();
    /**
     * Passed transitions data.
     */
    private final TransitionLogData transitionData;
    /**
     * Process definition data.
     */
    private final ProcessInstanceData processDefinitionData;

    public EmbeddedSubprocessLogsData(List<ProcessLog> processLogs, TransitionLogData transitionData, ProcessInstanceData processDefinition) {
        this.transitionData = transitionData;
        this.processDefinitionData = processDefinition;
        /*
         * if (correctLogsTokenId(processLogs)) {
         * processLogsByTokenId(processLogs); } else {
         */
        processLogsWithoutTokenId(processLogs);
        // }
    }

    private void processLogsWithoutTokenId(List<ProcessLog> processLogs) {
        String lastEmbeddedSubprocess = null;
        for (ProcessLog log : processLogs) {
            if (!Strings.isNullOrEmpty(log.getNodeId())) {
                lastEmbeddedSubprocess = processDefinitionData.checkEmbeddedSubprocess(log.getNodeId());
            }
            if (lastEmbeddedSubprocess == null) {
                this.processLogsWithoutEmbedded.add(log);
            } else {
                ArrayList<ProcessLog> logs = embeddedSubprocessesLogs.get(lastEmbeddedSubprocess);
                if (logs == null) {
                    logs = new ArrayList<ProcessLog>();
                    embeddedSubprocessesLogs.put(lastEmbeddedSubprocess, logs);
                }
                logs.add(log);
            }
        }
    }

    private boolean correctLogsTokenId(List<ProcessLog> processLogs) {
        for (String nodeId : processDefinitionData.getCreateTokenNodes()) {
            ArrayList<TransitionLog> logsFromNode = transitionData.getTransitionLogsFromNode(nodeId);
            if (logsFromNode == null || logsFromNode.size() == 0) {
                continue;
            }
            HashSet<Long> tokens = new HashSet<Long>();
            for (TransitionLog log : logsFromNode) {
                tokens.add(log.getTokenId());
            }
            if (tokens.size() != logsFromNode.size()) {
                return false;
            }
        }
        return true;
    }

    private void processLogsByTokenId(List<ProcessLog> processLogs) {
        HashMap<Long, Stack<String>> tokenIdToSubprocess = new HashMap<Long, Stack<String>>();
        for (ProcessLog log : processLogs) {
            Stack<String> subprocess = tokenIdToSubprocess.get(log.getTokenId());
            if (subprocess == null || subprocess.isEmpty()) {
                consumeProcessLog(log, tokenIdToSubprocess);
            } else {
                consumeSubprocessLog(subprocess.peek(), log, tokenIdToSubprocess);
            }
        }
    }

    private void consumeProcessLog(ProcessLog log, HashMap<Long, Stack<String>> tokenIdToSubprocess) {
        processLogsWithoutEmbedded.add(log);
        if (log instanceof NodeEnterLog && NodeType.SUBPROCESS == ((NodeEnterLog) log).getNodeType()) {
            SubprocessNode node = getSubprocessNode(log.getNodeId());
            if (node != null && node.isEmbedded()) {
                ParsedSubprocessDefinition subprocessDefinition = processDefinitionData.getEmbeddedSubprocess(node.getSubProcessName());
                String subProcessName = subprocessDefinition.getNodeId();
                Stack<String> stack = new Stack<String>();
                stack.push(subProcessName);
                tokenIdToSubprocess.put(log.getTokenId(), stack);
            }
        }
    }

    private void consumeSubprocessLog(String subprocessName, ProcessLog log, HashMap<Long, Stack<String>> tokenIdToSubprocess) {
        ArrayList<ProcessLog> logs = embeddedSubprocessesLogs.get(subprocessName);
        if (logs == null) {
            logs = new ArrayList<ProcessLog>();
            embeddedSubprocessesLogs.put(subprocessName, logs);
        }
        Stack<String> stack = tokenIdToSubprocess.get(log.getTokenId());
        if (log instanceof NodeEnterLog && NodeType.SUBPROCESS == ((NodeEnterLog) log).getNodeType()) {
            SubprocessNode node = getSubprocessNode(log.getNodeId());
            if (node != null && node.isEmbedded()) {
                ParsedSubprocessDefinition subprocessDefinition = processDefinitionData.getEmbeddedSubprocess(node.getSubProcessName());
                String newSubprocessName = subprocessDefinition.getNodeId();
                stack.push(newSubprocessName);
            }
        } else if (log instanceof NodeLeaveLog && NodeType.SUBPROCESS == ((NodeLeaveLog) log).getNodeType()) {
            stack.pop();
            if (stack.isEmpty()) {
                tokenIdToSubprocess.remove(log.getTokenId());
                processLogsWithoutEmbedded.add(log);
            } else {
                embeddedSubprocessesLogs.get(stack.peek()).add(log);
            }
        } else if (log instanceof NodeEnterLog && NodeType.FORK == ((NodeEnterLog) log).getNodeType()) {
            logs.add(log);
            List<TransitionLog> forkTransitions = transitionData.getTransitionLogsFromNode(log.getNodeId());
            if (forkTransitions != null) {
                for (TransitionLog transition : forkTransitions) {
                    Stack<String> stack2 = new Stack<String>();
                    stack.push(subprocessName);
                    tokenIdToSubprocess.put(transition.getTokenId(), stack2);
                }
            }
        } else {
            logs.add(log);
        }
    }

    private SubprocessNode getSubprocessNode(String nodeId) {
        Node result = processDefinitionData.getNode(nodeId);
        if (result == null || !(result instanceof SubprocessNode)) {
            return null;
        }
        return (SubprocessNode) result;
    }

    public List<ProcessLog> getProcessLogs(String subProcessId) {
        boolean isForEmbeddedSubprocess = subProcessId != null && !"null".equals(subProcessId);
        return isForEmbeddedSubprocess ? embeddedSubprocessesLogs.get(subProcessId) : processLogsWithoutEmbedded;
    }
}
