package ru.runa.wfe.task.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import ru.runa.wfe.task.TaskFormDraft;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class WfTaskFormDraft {
    private Long taskId;
    private Long actorId;
    private String dataB64;

    public WfTaskFormDraft(TaskFormDraft taskFormDraft) {
        this.taskId = taskFormDraft.getTaskId();
        this.actorId = taskFormDraft.getActorId();
        this.dataB64 = taskFormDraft.getDataB64();
    }
}
