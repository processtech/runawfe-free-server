package ru.runa.wfe.job;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import ru.runa.wfe.execution.CurrentProcess;

@Entity
@DiscriminatorValue(value = "I")
public abstract class InProcessTimerJob extends TimerJob {
    private String name;
    private CurrentProcess process;

    public InProcessTimerJob() {
    }

    public InProcessTimerJob(CurrentProcess process) {
        this.process = process;
        this.createDate = new Date();
    }

    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    public CurrentProcess getProcess() {
        return process;
    }

    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

}
