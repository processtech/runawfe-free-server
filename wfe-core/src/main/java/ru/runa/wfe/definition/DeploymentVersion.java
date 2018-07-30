package ru.runa.wfe.definition;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.user.Actor;

@Entity
@Table(name = "BPM_DEFINITION_VERSION")
public class DeploymentVersion implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Deployment deployment;
    private Long version;
    private byte[] content;
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

    @ManyToOne(targetEntity = Deployment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "DEFINITION_ID", nullable = false)
    @ForeignKey(name = "FK_VERSION_DEFINITION")
    @Index(name = "IX_VERSION_DEFINITION")
    public Deployment getDeployment() {
        return deployment;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
    }

    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
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

    @Transient
    public DeploymentVersion getCopy() {
        DeploymentVersion o = new DeploymentVersion();
        o.id = id;

        o.category = category;
        o.content = content;
        o.createDate = createDate;
        o.description = description;
        o.language = language;
        o.name = name;
        o.subprocessBindingDate = subprocessBindingDate;
        o.version = version;
        return o;
    }
}
