package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ExecAction")
public class ExecutorActionLog extends SystemLog {
    private String actionInfo;
    
    public ExecutorActionLog() {
    }

    public ExecutorActionLog(Long actorId, String actionInfo) {
        super(actorId);
        this.actionInfo = actionInfo;
    }
    
    @Column(name = "PROCESS_DEFINITION_NAME", length = 1024)
    public String getActionInfo() {
        return actionInfo;
    }

    public void setActionInfo(String actionInfo) {
        this.actionInfo = actionInfo;
    }
}