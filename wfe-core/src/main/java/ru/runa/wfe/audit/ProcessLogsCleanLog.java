package ru.runa.wfe.audit;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PLDel")
public class ProcessLogsCleanLog extends SystemLog {
    private Date beforeDate;

    protected ProcessLogsCleanLog() {
    }

    public ProcessLogsCleanLog(Long actorId, Date beforeDate) {
        super(actorId);
        this.beforeDate = beforeDate;
    }
    
    @Column(name = "PROCESS_LOG_CLEAN_BEFORE_DATE")
    public Date getBeforeDate() {
        return beforeDate;
    }
    
    public void setBeforeDate(Date beforeDate) {
        this.beforeDate = beforeDate;
    }
}
