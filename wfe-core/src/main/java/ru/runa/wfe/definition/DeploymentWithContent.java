package ru.runa.wfe.definition;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Actor;

/**
 * Workaround in order do not load blob from db when it is not needed.
 * 
 * @author dofs
 */
@Entity
@Table(name = "BPM_PROCESS_DEFINITION")
public class DeploymentWithContent extends SecuredObject {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long version;
    private Language language;
    private String name;
    private String description;
    private String category;
    private Date createDate;
    private Actor createActor;
    private Date updateDate;
    private Actor updateActor;
    private Date subprocessBindingDate;
    private byte[] content;

    public DeploymentWithContent() {
    }

    public DeploymentWithContent(Deployment deployment, byte[] content) {
        setCategory(deployment.getCategory());
        setCreateActor(deployment.getCreateActor());
        setCreateDate(deployment.getCreateDate());
        setDescription(deployment.getDescription());
        setId(deployment.getId());
        setLanguage(deployment.getLanguage());
        setName(deployment.getName());
        setSubprocessBindingDate(deployment.getSubprocessBindingDate());
        setVersion(deployment.getVersion());
        setContent(content);
        setUpdateActor(deployment.getUpdateActor());
        setUpdateDate(deployment.getUpdateDate());
    }

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

    @Column(name = "VERSION", nullable = false)
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "LANGUAGE", nullable = false, length = 1024)
    @Enumerated(value = EnumType.STRING)
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "NAME", nullable = false, length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION", length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CATEGORY", nullable = false, length = 1024)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    @Lob
    @Column(length = 16777216, name = "BYTES")
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Transient
    @Override
    public Long getIdentifiableId() {
        return (long) getName().hashCode();
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DEFINITION;
    }

    @Transient
    public String[] getCategories() {
        if (category != null) {
            return category.split(Utils.CATEGORY_DELIMITER);
        }
        return new String[] {};
    }

    public void setCategories(List<String> categories) {
        category = Joiner.on(Utils.CATEGORY_DELIMITER).join(categories);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeploymentWithContent) {
            DeploymentWithContent d = (DeploymentWithContent) obj;
            return Objects.equal(name, d.name) && Objects.equal(version, d.version);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("version", version).toString();
    }

}
