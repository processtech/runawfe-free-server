package ru.runa.wfe.job;

import com.google.common.base.Objects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.CurrentProcess;

@Entity
@Table(name = "BPM_JOB")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "J")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Job {
    private Long id;
    private Long version;
    private String name;
    private String dueDateExpression;
    private Date dueDate;
    private CurrentProcess process;
    private CurrentToken token;
    private Date createDate;

    public Job() {
    }

    public Job(CurrentToken token) {
        this.token = token;
        this.process = token.getProcess();
        this.createDate = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_JOB", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    protected Long getVersion() {
        return version;
    }

    protected void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DUE_DATE_EXPRESSION")
    public String getDueDateExpression() {
        return dueDateExpression;
    }

    public void setDueDateExpression(String dueDateExpression) {
        this.dueDateExpression = dueDateExpression;
    }

    @Column(name = "DUE_DATE")
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_JOB_PROCESS")
    @Index(name = "IX_JOB_PROCESS")
    public CurrentProcess getProcess() {
        return process;
    }

    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "TOKEN_ID")
    @ForeignKey(name = "FK_JOB_TOKEN")
    public CurrentToken getToken() {
        return token;
    }

    public void setToken(CurrentToken token) {
        this.token = token;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public abstract void execute(ExecutionContext executionContext);

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("name", name).add("dueDate", CalendarUtil.formatDateTime(dueDate))
                .add("process", getProcess()).toString();
    }

}
