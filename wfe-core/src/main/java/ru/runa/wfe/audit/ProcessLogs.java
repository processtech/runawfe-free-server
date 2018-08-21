package ru.runa.wfe.audit;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.SafeIndefiniteLoop;
import ru.runa.wfe.lang.NodeType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessLogs implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<IProcessLog> logs = Lists.newArrayList();
    @XmlTransient
    private final HashMap<Long, Long> subprocessToProcessIds = Maps.newHashMap();

    public ProcessLogs() {
    }

    public ProcessLogs(Long processId) {
        subprocessToProcessIds.put(processId, null);
    }

    public void addLogs(List<IProcessLog> processLogs, boolean withSubprocesses) {
        logs.addAll(processLogs);
        if (withSubprocesses) {
            for (IProcessLog log : processLogs) {
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

    public List<IProcessLog> getLogs() {
        return logs;
    }

    public <T extends IProcessLog> T getFirstOrNull(Class<T> logClass) {
        for (IProcessLog log : logs) {
            if (logClass.isAssignableFrom(log.getClass())) {
                return (T) log;
            }
        }
        return null;
    }

    public <T extends IProcessLog> T getLastOrNull(Class<T> logClass) {
        for (IProcessLog log : Lists.reverse(logs)) {
            if (logClass.isAssignableFrom(log.getClass())) {
                return (T) log;
            }
        }
        return null;
    }

    public <T extends IProcessLog> List<T> getLogs(Class<T> logClass) {
        List<T> list = Lists.newArrayList();
        for (IProcessLog log : logs) {
            if (logClass.isAssignableFrom(log.getClass())) {
                list.add((T) log);
            }
        }
        return list;
    }

    public List<IProcessLog> getLogs(String nodeId) {
        List<IProcessLog> list = Lists.newArrayList();
        for (IProcessLog log : logs) {
            if (Objects.equal(log.getNodeId(), nodeId)) {
                list.add(log);
            }
        }
        return list;
    }

    public Map<TaskCreateLog, TaskEndLog> getTaskLogs() {
        Map<String, TaskCreateLog> tmpByTaskName = Maps.newHashMap();
        Map<Long, TaskCreateLog> tmpByTaskId = Maps.newHashMap();
        Map<TaskCreateLog, TaskEndLog> result = Maps.newHashMap();
        boolean compatibilityMode = false;
        for (IProcessLog log : logs) {
            if (log instanceof TaskCreateLog) {
                TaskCreateLog taskCreateLog = (TaskCreateLog) log;
                String key = log.getProcessId() + taskCreateLog.getTaskName();
                tmpByTaskName.put(key, taskCreateLog);
                tmpByTaskId.put(taskCreateLog.getTaskId(), taskCreateLog);
            }
            if (log instanceof TaskEndLog) {
                TaskEndLog taskEndLog = (TaskEndLog) log;
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
                    TaskCreateLog taskCreateLog = new TaskCreateLog();
                    taskCreateLog.setId(processStartLog.getId());
                    taskCreateLog.setCreateDate(nodeLeaveLog.getCreateDate());
                    taskCreateLog.setProcessId(nodeLeaveLog.getProcessId());
                    taskCreateLog.setSeverity(nodeLeaveLog.getSeverity());
                    taskCreateLog.setTokenId(nodeLeaveLog.getTokenId());
                    taskCreateLog.addAttribute(Attributes.ATTR_TASK_NAME, nodeLeaveLog.getNodeName());
                    TaskEndLog taskEndLog = new TaskEndLog();
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
