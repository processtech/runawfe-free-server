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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.lang.Node;

@Entity
@Table(name = "BPM_SUBPROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CurrentNodeProcess extends NodeProcess<CurrentProcess, CurrentToken> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SUBPROCESS", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    private CurrentProcess process;

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    private CurrentProcess subProcess;

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "ROOT_PROCESS_ID", nullable = false)
    private CurrentProcess rootProcess;

    @ManyToOne(targetEntity = CurrentToken.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    private CurrentToken parentToken;

    protected CurrentNodeProcess() {
    }

    public CurrentNodeProcess(Node processStateNode, CurrentToken parentToken, CurrentProcess rootProcess, CurrentProcess subProcess, Integer index) {
        this.process = parentToken.getProcess();
        this.parentToken = parentToken;
        this.nodeId = processStateNode.getNodeId();
        this.subProcess = subProcess;
        this.rootProcess = rootProcess;
        this.index = index;
        this.createDate = new Date();
    }

    @Override
    public boolean isArchive() {
        return false;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setIndex(Integer index) {
        this.index = index;
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
    public CurrentProcess getSubProcess() {
        return subProcess;
    }

    public void setSubProcess(CurrentProcess subProcess) {
        this.subProcess = subProcess;
    }

    @Override
    public CurrentProcess getRootProcess() {
        return rootProcess;
    }

    public void setRootProcess(CurrentProcess rootProcess) {
        this.rootProcess = rootProcess;
    }

    @Override
    public CurrentToken getParentToken() {
        return parentToken;
    }

    public void setParentToken(CurrentToken parentToken) {
        this.parentToken = parentToken;
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
