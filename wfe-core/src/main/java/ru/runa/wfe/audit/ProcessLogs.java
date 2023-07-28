package ru.runa.wfe.audit;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import lombok.val;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.SafeIndefiniteLoop;

@XmlAccessorType(XmlAccessType.FIELD)
@CommonsLog
public class ProcessLogs implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<BaseProcessLog> logs = new ArrayList<>();
    @XmlTransient
    private final HashMap<Long, Long> subprocessToProcessIds = new HashMap<>();

    public ProcessLogs() {
    }

    public ProcessLogs(Long processId) {
        subprocessToProcessIds.put(processId, null);
    }

    public void addLogs(List<? extends BaseProcessLog> processLogs, boolean withSubprocesses) {
        logs.addAll(processLogs);
        if (withSubprocesses) {
            for (BaseProcessLog processLog : processLogs) {
                if (processLog instanceof SubprocessStartLog) {
                    Long subprocessId = ((SubprocessStartLog) processLog).getSubprocessId();
                    subprocessToProcessIds.put(subprocessId, processLog.getProcessId());
                }
            }
        }
        Collections.sort(logs);
    }

    public int getMaxSubprocessLevel() {
        val tmpIds = new HashMap<Long, Long>(subprocessToProcessIds);
        val levels = new HashMap<Long, Integer>();
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

    public List<Long> getSubprocessIds(BaseProcessLog processLog) {
        val result = new ArrayList<Long>();
        Long processId = processLog.getProcessId();
        while (subprocessToProcessIds.get(processId) != null) {
            result.add(processId);
            processId = subprocessToProcessIds.get(processId);
        }
        Collections.reverse(result);
        return result;
    }

    public List<BaseProcessLog> getLogs() {
        return logs;
    }

    public <T extends ProcessLog> T getFirstOrNull(Class<T> logClass) {
        for (ProcessLog processLog : logs) {
            if (logClass.isAssignableFrom(processLog.getClass())) {
                return (T) processLog;
            }
        }
        return null;
    }

    public <T extends ProcessLog> T getLastOrNull(Class<T> logClass) {
        for (ProcessLog processLog : Lists.reverse(logs)) {
            if (logClass.isAssignableFrom(processLog.getClass())) {
                return (T) processLog;
            }
        }
        return null;
    }

    public <T extends ProcessLog> List<T> getLogs(Class<T> logClass) {
        val list = new ArrayList<T>();
        for (ProcessLog processLog : logs) {
            if (logClass.isAssignableFrom(processLog.getClass())) {
                list.add((T) processLog);
            }
        }
        return list;
    }

    public List<BaseProcessLog> getLogs(String nodeId) {
        val list = new ArrayList<BaseProcessLog>();
        for (BaseProcessLog processLog : logs) {
            if (Objects.equal(processLog.getNodeId(), nodeId)) {
                list.add(processLog);
            }
        }
        return list;
    }

    public Map<TaskCreateLog, TaskEndLog> getTaskLogs() {
        Map<Long, TaskCreateLog> taskCreateLogByTaskId = Maps.newHashMap();
        Map<TaskCreateLog, TaskEndLog> result = Maps.newHashMap();
        for (BaseProcessLog processLog : logs) {
            if (processLog instanceof TaskCreateLog) {
                TaskCreateLog taskCreateLog = (TaskCreateLog) processLog;
                taskCreateLogByTaskId.put(taskCreateLog.getTaskId(), taskCreateLog);
            }
            if (processLog instanceof TaskEndLog) {
                TaskEndLog taskEndLog = (TaskEndLog) processLog;
                TaskCreateLog taskCreateLog = null;
                if (taskCreateLogByTaskId.containsKey(taskEndLog.getTaskId())) {
                    taskCreateLog = taskCreateLogByTaskId.remove(taskEndLog.getTaskId());
                }
                if (taskCreateLog == null) {
                    log.warn("No TaskCreateLog for " + processLog);
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
