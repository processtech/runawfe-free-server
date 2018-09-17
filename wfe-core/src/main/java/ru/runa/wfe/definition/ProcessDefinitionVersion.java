package ru.runa.wfe.definition;

import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.val;
import org.hibernate.annotations.ForeignKey;
import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "BPM_PROCESS_DEFINITION_VER", indexes = {
        @Index(name = "ix_version_definition_ver", columnList = "definition_id, version")
})
public class ProcessDefinitionVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private ProcessDefinition definition;
    private Long version;
    private Long subVersion;
    private byte[] content;
    private Date createDate;
    private Actor createActor;
    private Date updateDate;
    private Actor updateActor;
    private Date subprocessBindingDate;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS_DEFINITION_VER", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_VERSION_DEFINITION")
    public ProcessDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(ProcessDefinition definition) {
        this.definition = definition;
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

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 16777216, name = "BYTES")
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
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
    @ForeignKey(name = "FK_DEFINITION_CREATE_USER")
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
    @ForeignKey(name = "FK_DEFINITION_UPDATE_USER")
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
        if (this == o) return true;
        if (!(o instanceof ProcessDefinitionVersion)) return false;
        ProcessDefinitionVersion x = (ProcessDefinitionVersion) o;
        return Objects.equal(id, x.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("version", version).toString();
    }

    @Transient
    public ProcessDefinitionWithVersion createCopyWithDefinition() {
        val o = new ProcessDefinitionVersion();
        o.id = id;
        o.definition = definition.createCopy();
        o.version = version;
        o.content = content;
        o.createDate = createDate;
        o.subprocessBindingDate = subprocessBindingDate;
        return new ProcessDefinitionWithVersion(o.definition, o);
    }
}
