package ru.runa.wfe.job.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.lang.NodeType;

/**
 * Process job.
 *
 * @author Dofs
 * @since 4.3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfJob implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Long processId;
    private Long tokenId;
    private NodeType nodeType;
    private String nodeId;
    private Date createDate;
    private Date dueDate;
    private String dueDateExpression;

    public WfJob() {
    }

    public WfJob(Job job) {
        this.id = job.getId();
        this.name = job.getName();
        this.processId = job.getProcess().getId();
        this.tokenId = job.getToken().getId();
        this.nodeType = job.getToken().getNodeType();
        this.nodeId = job.getToken().getNodeId();
        this.createDate = job.getCreateDate();
        this.dueDate = job.getDueDate();
        this.dueDateExpression = job.getDueDateExpression();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getProcessId() {
        return processId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public String getDueDateExpression() {
        return dueDateExpression;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfJob) {
            return Objects.equal(id, ((WfJob) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("processId", processId).add("id", id).add("name", name).toString();
    }

}
