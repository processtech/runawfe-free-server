package ru.runa.wfe.graph.history;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ru.runa.wfe.audit.IProcessLog;

/**
 * Component to split logs to main process logs and embedded subprocesses logs.
 */
public class EmbeddedSubprocessLogsData {
    /**
     * Logs, belongs to process, except embedded subprocesses logs.
     */
    private final List<IProcessLog> processLogsWithoutEmbedded = Lists.newArrayList();
    /**
     * Maps from embedded subprocess to it's logs.
     */
    private final HashMap<String, ArrayList<IProcessLog>> embeddedSubprocessesLogs = new HashMap<>();
    /**
     * Process definition data.
     */
    private final ProcessInstanceData processDefinitionData;

    public EmbeddedSubprocessLogsData(List<? extends IProcessLog> processLogs, ProcessInstanceData processDefinition) {
        this.processDefinitionData = processDefinition;
        /*
         * if (correctLogsTokenId(processLogs)) {
         * processLogsByTokenId(processLogs); } else {
         */
        processLogsWithoutTokenId(processLogs);
        // }
    }

    private void processLogsWithoutTokenId(List<? extends IProcessLog> processLogs) {
        String lastEmbeddedSubprocess = null;
        for (IProcessLog log : processLogs) {
            if (!Strings.isNullOrEmpty(log.getNodeId())) {
                lastEmbeddedSubprocess = processDefinitionData.checkEmbeddedSubprocess(log.getNodeId());
            }
            if (lastEmbeddedSubprocess == null) {
                this.processLogsWithoutEmbedded.add(log);
            } else {
                ArrayList<IProcessLog> logs = embeddedSubprocessesLogs.get(lastEmbeddedSubprocess);
                if (logs == null) {
                    logs = new ArrayList<>();
                    embeddedSubprocessesLogs.put(lastEmbeddedSubprocess, logs);
                }
                logs.add(log);
            }
        }
    }

    public List<IProcessLog> getProcessLogs(String subProcessId) {
        boolean isForEmbeddedSubprocess = subProcessId != null && !"null".equals(subProcessId);
        return isForEmbeddedSubprocess ? embeddedSubprocessesLogs.get(subProcessId) : processLogsWithoutEmbedded;
    }
}
