package ru.runa.wfe.audit;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
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
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.SafeIndefiniteLoop;
import ru.runa.wfe.lang.NodeType;

@XmlAccessorType(XmlAccessType.FIELD)
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
            for (BaseProcessLog log : processLogs) {
                if (log instanceof SubprocessStartLog) {
                    Long subprocessId = ((SubprocessStartLog) log).getSubprocessId();
                    subprocessToProcessIds.put(subprocessId, log.getProcessId());
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
        val list = new ArrayList<T>();
        for (ProcessLog log : logs) {
            if (logClass.isAssignableFrom(log.getClass())) {
                list.add((T) log);
            }
        }
        return list;
    }

    public List<BaseProcessLog> getLogs(String nodeId) {
        val list = new ArrayList<BaseProcessLog>();
        for (BaseProcessLog log : logs) {
            if (Objects.equal(log.getNodeId(), nodeId)) {
                list.add(log);
            }
        }
        return list;
    }

    public Map<TaskCreateLog, TaskEndLog> getTaskLogs() {
        val tmpByTaskName = new HashMap<String, TaskCreateLog>();
        val tmpByTaskId = new HashMap<Long, TaskCreateLog>();
        val result = new HashMap<TaskCreateLog, TaskEndLog>();
        boolean compatibilityMode = false;
        for (BaseProcessLog log : logs) {
            if (log instanceof TaskCreateLog) {
                val taskCreateLog = (TaskCreateLog) log;
                String key = log.getProcessId() + taskCreateLog.getTaskName();
                tmpByTaskName.put(key, taskCreateLog);
                tmpByTaskId.put(taskCreateLog.getTaskId(), taskCreateLog);
            }
            if (log instanceof TaskEndLog) {
                val taskEndLog = (TaskEndLog) log;
                TaskCreateLog taskCreateLog;
                if (taskEndLog.getTaskId() != null && tmpByTaskId.containsKey(taskEndLog.getTaskId())) {
                    taskCreateLog = tmpByTaskId.remove(taskEndLog.getTaskId());
                    tmpByTaskName.remove(log.getProcessId() + taskCreateLog.getTaskName());
                } else {
                    String key = log.getProcessId() + taskEndLog.getTaskName();
                    taskCreateLog = tmpByTaskName.remove(key);
                    compatibilityMode = true;
                }
                if (taskCreateLog == null) {
                    LogFactory.getLog(getClass()).warn("No TaskCreateLog for " + log);
                    continue;
                }
                result.put(taskCreateLog, taskEndLog);
            }
            if (log instanceof NodeLeaveLog) {
                NodeLeaveLog nodeLeaveLog = (NodeLeaveLog) log;
                if (NodeType.START_EVENT == nodeLeaveLog.getNodeType()) {
                    ProcessStartLog processStartLog = getFirstOrNull(ProcessStartLog.class);
                    if (processStartLog == null) {
                        continue;
                    }
                    // These fake temporary entities are never stored. TODO Can we get rid of them and then make CurrentProcessLog.setId() private?
                    CurrentTaskCreateLog taskCreateLog = new CurrentTaskCreateLog();
                    taskCreateLog.setId(processStartLog.getId());
                    taskCreateLog.setCreateDate(nodeLeaveLog.getCreateDate());
                    taskCreateLog.setProcessId(nodeLeaveLog.getProcessId());
                    taskCreateLog.setSeverity(nodeLeaveLog.getSeverity());
                    taskCreateLog.setTokenId(nodeLeaveLog.getTokenId());
                    taskCreateLog.addAttribute(Attributes.ATTR_TASK_NAME, nodeLeaveLog.getNodeName());
                    CurrentTaskEndLog taskEndLog = new CurrentTaskEndLog();
                    taskEndLog.setId(processStartLog.getId());
                    taskEndLog.setCreateDate(nodeLeaveLog.getCreateDate());
                    taskEndLog.setProcessId(nodeLeaveLog.getProcessId());
                    taskEndLog.setSeverity(nodeLeaveLog.getSeverity());
                    taskEndLog.setTokenId(nodeLeaveLog.getTokenId());
                    taskEndLog.addAttribute(Attributes.ATTR_TASK_NAME, nodeLeaveLog.getNodeName());
                    taskEndLog.addAttribute(Attributes.ATTR_ACTOR_NAME, processStartLog.getActorName());
                    result.put(taskCreateLog, taskEndLog);
                }
            }
        }
        // unfinished tasks
        for (TaskCreateLog taskCreateLog : compatibilityMode ? tmpByTaskName.values() : tmpByTaskId.values()) {
            result.put(taskCreateLog, null);
        }
        return result;
    }
}
