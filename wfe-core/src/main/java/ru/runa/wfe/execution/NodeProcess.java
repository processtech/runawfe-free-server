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
public class NodeProcess extends BaseNodeProcess<Process, Token> {

    private Long id;
    private Process process;
    private Token parentToken;
    private Process subProcess;

    protected NodeProcess() {
    }

    public NodeProcess(Node processStateNode, Token parentToken, Process subProcess, Integer index) {
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
    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PARENT_PROCESS")
    @Index(name = "IX_SUBPROCESS_PARENT_PROCESS")
    public Process getProcess() {
        return process;
    }

    @Override
    public void setProcess(Process process) {
        this.process = process;
    }

    @Override
    @ManyToOne(targetEntity = Token.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @ForeignKey(name = "FK_SUBPROCESS_TOKEN")
    public Token getParentToken() {
        return parentToken;
    }

    @Override
    public void setParentToken(Token parentToken) {
        this.parentToken = parentToken;
    }

    @Override
    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PROCESS")
    @Index(name = "IX_SUBPROCESS_PROCESS")
    public Process getSubProcess() {
        return subProcess;
    }

    @Override
    public void setSubProcess(Process subProcess) {
        this.subProcess = subProcess;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NodeProcess) {
            NodeProcess b = (NodeProcess) obj;
            return Objects.equal(id, b.id);
        }
        return super.equals(obj);
    }
}
