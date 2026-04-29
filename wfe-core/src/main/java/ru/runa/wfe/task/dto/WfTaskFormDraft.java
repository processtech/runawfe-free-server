package ru.runa.wfe.task.dto;

import lombok.Getter;
import ru.runa.wfe.task.TaskFormDraft;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class WfTaskFormDraft {
    private Long taskId;
    private Long actorId;
    private byte[] data;

    public WfTaskFormDraft(TaskFormDraft taskFormDraft) {
        this.taskId = taskFormDraft.getTaskId();
        this.actorId = taskFormDraft.getActorId();
        this.data = taskFormDraft.getData();
    }
}
