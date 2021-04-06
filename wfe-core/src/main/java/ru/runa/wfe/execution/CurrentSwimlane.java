package ru.runa.wfe.execution;

import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.audit.CurrentSwimlaneAssignLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.extension.Assignable;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;

/**
 * is a process role for a one process.
 */
@Entity
@Table(name = "BPM_SWIMLANE")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@CommonsLog
public class CurrentSwimlane extends Swimlane<CurrentProcess> implements Serializable, Assignable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SWIMLANE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    private CurrentProcess process;

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID")
    private Executor executor;

    public CurrentSwimlane() {
    }

    public CurrentSwimlane(String name) {
        this.name = name;
        this.createDate = new Date();
    }

    @Override
    public boolean isArchived() {
        return false;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public CurrentProcess getProcess() {
        return process;
    }

    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public String getSwimlaneName() {
        return name;
    }

    @Override
    public void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate) {
        if (!Objects.equal(getExecutor(), executor)) {
            log.debug("assigning swimlane '" + getName() + "' to '" + executor + "'");
            executionContext.addLog(new CurrentSwimlaneAssignLog(this, executor));
            setExecutor(executor);
        }
        if (cascadeUpdate) {
            // change actor for already assigned tasks
            for (Task task : ApplicationContextFactory.getTaskDao().findByProcessAndSwimlane(process, this)) {
                task.assignExecutor(executionContext, executor, false);
            }
        }
    }
}
