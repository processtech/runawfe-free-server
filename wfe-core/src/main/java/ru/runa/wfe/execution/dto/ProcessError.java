package ru.runa.wfe.execution.dto;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.bot.BotTask;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessError implements Serializable {
    private Long processId;
    private String nodeId;
    private String taskName;
    private BotTask botTask;
    private Date occurredDate = new Date();
    private String throwableMessage;
    private String throwableDetails;

    public ProcessError() {
    }

    public ProcessError(Long processId, String nodeId) {
        this.processId = processId;
        this.nodeId = nodeId;
    }

    public ProcessError(Long processId, String nodeId, String taskName, BotTask botTask, Throwable throwable) {
        this(processId, nodeId);
        this.taskName = taskName;
        this.botTask = botTask;
        if (throwable != null) {
            this.throwableMessage = throwable.getLocalizedMessage();
            if (Strings.isNullOrEmpty(throwableMessage)) {
                throwableMessage = throwable.getClass().getName();
            }
            this.throwableDetails = Throwables.getStackTraceAsString(throwable);
        }
    }

    public Long getProcessId() {
        return processId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getTaskName() {
        return taskName;
    }

    public BotTask getBotTask() {
        return botTask;
    }

    public Date getOccurredDate() {
        return occurredDate;
    }

    public String getThrowableMessage() {
        return throwableMessage;
    }

    public String getThrowableDetails() {
        return throwableDetails;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(processId, nodeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessError) {
            ProcessError bti = (ProcessError) obj;
            return Objects.equal(processId, bti.processId) && Objects.equal(nodeId, bti.nodeId);
        }
        return super.equals(obj);
    }
}