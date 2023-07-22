package ru.runa.wfe.execution;

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
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import ru.runa.wfe.lang.Node;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import ru.runa.wfe.lang.SubprocessNode;

@Entity
@Table(name = "BPM_SUBPROCESS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NodeProcess {
    private Long id;
    private Process process;
    private Token parentToken;
    private String nodeId;
    private Process subProcess;
    // TODO why = 0 for subprocess?
    private Integer index;
    private Date createDate;
    private Boolean async;

    protected NodeProcess() {
    }

    public NodeProcess(SubprocessNode processStateNode, Token parentToken, Process subProcess, Integer index) {
        this.process = parentToken.getProcess();
        this.parentToken = parentToken;
        this.nodeId = processStateNode.getNodeId();
        this.subProcess = subProcess;
        this.index = index;
        this.createDate = new Date();
        this.async = processStateNode.isAsync();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_SUBPROCESS", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PARENT_PROCESS")
    @Index(name = "IX_SUBPROCESS_PARENT_PROCESS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    @ManyToOne(targetEntity = Token.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_TOKEN_ID")
    @ForeignKey(name = "FK_SUBPROCESS_TOKEN")
    public Token getParentToken() {
        return parentToken;
    }

    public void setParentToken(Token parentToken) {
        this.parentToken = parentToken;
    }

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_SUBPROCESS_PROCESS")
    @Index(name = "IX_SUBPROCESS_PROCESS")
    public Process getSubProcess() {
        return subProcess;
    }

    public void setSubProcess(Process subProcess) {
        this.subProcess = subProcess;
    }

    @Column(name = "PARENT_NODE_ID", length = 1024)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "SUBPROCESS_INDEX")
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "ASYNC")
    public Boolean isAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("nodeId", nodeId).add("process", process).add("subProcess", subProcess).toString();
    }

}
