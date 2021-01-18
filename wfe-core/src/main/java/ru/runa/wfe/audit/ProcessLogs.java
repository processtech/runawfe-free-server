package ru.runa.wfe.audit;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.SafeIndefiniteLoop;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessLogs implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<ProcessLog> logs = Lists.newArrayList();
    @XmlTransient
    private final HashMap<Long, Long> subprocessToProcessIds = Maps.newHashMap();

    public ProcessLogs() {
    }

    public ProcessLogs(Long processId) {
        subprocessToProcessIds.put(processId, null);
    }

    public void addLogs(List<ProcessLog> processLogs, boolean withSubprocesses) {
        logs.addAll(processLogs);
        if (withSubprocesses) {
            for (ProcessLog log : processLogs) {
                if (log instanceof SubprocessStartLog) {
                    Long subprocessId = ((SubprocessStartLog) log).getSubprocessId();
                    subprocessToProcessIds.put(subprocessId, log.getProcessId());
                }
            }
        }
        Collections.sort(logs);
    }

    public int getMaxSubprocessLevel() {
        final Map<Long, Long> tmpIds = Maps.newHashMap(subprocessToProcessIds);
        final Map<Long, Integer> levels = Maps.newHashMap();
        new SafeIndefiniteLoop(100) {

            @Override
            protected boolean continueLoop() {
                return !tmpIds.isEmpty();
            }

            @Override
            protected void doOp() {
                for (Map.Entry<Long, Long> entry : tmpIds.entrySet()) {
                    if (levels.containsKey(entry.getKey())) {
                        continue;
                    }
                    if (entry.getValue() == null) {
                        levels.put(entry.getKey(), 0);
                        tmpIds.remove(entry.getKey());
                        break;
                    }
                    if (levels.containsKey(entry.getValue())) {
                        levels.put(entry.getKey(), levels.get(entry.getValue()) + 1);
                        tmpIds.remove(entry.getKey());
                        break;
                    }
                }
            }

        }.doLoop();
        int level = 0;
        for (Integer l : levels.values()) {
            if (l > level) {
                level = l;
            }
        }
        return level;
    }

    public List<Long> getSubprocessIds(ProcessLog processLog) {
        List<Long> result = Lists.newArrayList();
        Long processId = processLog.getProcessId();
        while (subprocessToProcessIds.get(processId) != null) {
            result.add(processId);
            processId = subprocessToProcessIds.get(processId);
        }
        Collections.reverse(result);
        return result;
    }

    public List<ProcessLog> getLogs() {
        return logs;
    }

    public <T extends ProcessLog> T getFirstOrNull(Class<T> logClass) {
        for (ProcessLog log : logs) {
            if (logClass.isAssignableFrom(log.getClass())) {
                return (T) log;
            }
        }
        return null;
    }

    public <T extends ProcessLog> T getLastOrNull(Class<T> logClass) {
        for (ProcessLog log : Lists.reverse(logs)) {
            if (logClass.isAssignableFrom(log.getClass())) {
                return (T) log;
            }
        }
        return null;
    }



    public <T extends ProcessLog> List<T> getLogs(Class<T> logClass) {
        List<T> list = Lists.newArrayList();
        for (ProcessLog log : logs) {
            if (logClass.isAssignableFrom(log.getClass())) {
                list.add((T) log);
            }
        }
        return list;
    }

    public List<ProcessLog> getLogs(String nodeId) {
        List<ProcessLog> list = Lists.newArrayList();
        for (ProcessLog log : logs) {
            if (Objects.equal(log.getNodeId(), nodeId)) {
                list.add(log);
            }
        }
        return list;
    }

    public Map<TaskCreateLog, TaskEndLog> getTaskLogs() {
        Map<Long, TaskCreateLog> taskCreateLogByTaskId = Maps.newHashMap();
        Map<TaskCreateLog, TaskEndLog> result = Maps.newHashMap();
        for (ProcessLog log : logs) {
            if (log instanceof TaskCreateLog) {
                TaskCreateLog taskCreateLog = (TaskCreateLog) log;
                taskCreateLogByTaskId.put(taskCreateLog.getTaskId(), taskCreateLog);
            }
            if (log instanceof TaskEndLog) {
                TaskEndLog taskEndLog = (TaskEndLog) log;
                TaskCreateLog taskCreateLog = null;
                if (taskCreateLogByTaskId.containsKey(taskEndLog.getTaskId())) {
                    taskCreateLog = taskCreateLogByTaskId.remove(taskEndLog.getTaskId());
                }
                if (taskCreateLog == null) {
                    LogFactory.getLog(getClass()).warn("No TaskCreateLog for " + log);
                    continue;
                }
                result.put(taskCreateLog, taskEndLog);
            }
        }
        // unfinished tasks
        for (TaskCreateLog taskCreateLog : taskCreateLogByTaskId.values()) {
            result.put(taskCreateLog, null);
        }
        return result;
    }
}
