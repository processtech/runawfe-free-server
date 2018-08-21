package ru.runa.wfe.execution;

import com.google.common.base.Objects;
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
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.lang.Node;

@Entity
@Table(name = "BPM_SUBPROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CurrentNodeProcess extends NodeProcess<CurrentProcess, CurrentToken> {

    private Long id;
    private CurrentProcess process;
    private CurrentToken parentToken;
    private CurrentProcess subProcess;

    protected CurrentNodeProcess() {
    }

    public CurrentNodeProcess(Node processStateNode, CurrentToken parentToken, CurrentProcess subProcess, Integer index) {
        this.process = parentToken.getProcess();
        this.parentToken = parentToken;
        this.nodeId = processStateNode.getNodeId();
        this.subProcess = subProcess;
        this.index = index;
        this.createDate = new Date();
    }

    @Override
    @Transient
    public boolean isArchive() {
        return false;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SUBPROCESS", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PARENT_PROCESS")
    @Index(name = "IX_SUBPROCESS_PARENT_PROCESS")
    public CurrentProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @ForeignKey(name = "FK_SUBPROCESS_TOKEN")
    public CurrentToken getParentToken() {
        return parentToken;
    }

    @Override
    public void setParentToken(CurrentToken parentToken) {
        this.parentToken = parentToken;
    }

    @Override
    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PROCESS")
    @Index(name = "IX_SUBPROCESS_PROCESS")
    public CurrentProcess getSubProcess() {
        return subProcess;
    }

    @Override
    public void setSubProcess(CurrentProcess subProcess) {
        this.subProcess = subProcess;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CurrentNodeProcess) {
            CurrentNodeProcess b = (CurrentNodeProcess) obj;
            return Objects.equal(id, b.id);
        }
        return super.equals(obj);
    }
}
