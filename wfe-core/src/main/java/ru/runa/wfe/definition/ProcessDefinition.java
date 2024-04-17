package ru.runa.wfe.definition;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "BPM_PROCESS_DEFINITION")
public class ProcessDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private ProcessDefinitionPack pack;
    private Long version;
    private Long subVersion;
    private Date createDate;
    private Actor createActor;
    private Date updateDate;
    private Actor updateActor;
    private Date subprocessBindingDate;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS_DEFINITION", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "PACK_ID", nullable = false)
    public ProcessDefinitionPack getPack() {
        return pack;
    }

    public void setPack(ProcessDefinitionPack pack) {
        this.pack = pack;
    }

    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "SUBVERSION", nullable = false)
    public Long getSubVersion() {
        return subVersion;
    }

    public void setSubVersion(Long subVersion) {
        this.subVersion = subVersion;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @ManyToOne
    @JoinColumn(name = "CREATE_USER_ID")
    public Actor getCreateActor() {
        return createActor;
    }

    public void setCreateActor(Actor createActor) {
        this.createActor = createActor;
    }

    @Column(name = "UPDATE_DATE")
    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @ManyToOne
    @JoinColumn(name = "UPDATE_USER_ID")
    public Actor getUpdateActor() {
        return updateActor;
    }

    public void setUpdateActor(Actor updateActor) {
        this.updateActor = updateActor;
    }

    @Column(name = "SUBPROCESS_BINDING_DATE")
    public Date getSubprocessBindingDate() {
        return subprocessBindingDate;
    }

    public void setSubprocessBindingDate(Date subprocessBindingDate) {
        this.subprocessBindingDate = subprocessBindingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessDefinition)) {
            return false;
        }
        ProcessDefinition x = (ProcessDefinition) o;
        return Objects.equal(id, x.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("version", version).toString();
    }

    @Transient
    public ProcessDefinition getCopy() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCreateDate(getCreateDate());
        processDefinition.setId(getId());
        processDefinition.setSubprocessBindingDate(getSubprocessBindingDate());
        processDefinition.setVersion(getVersion());
        return processDefinition;
    }
}
