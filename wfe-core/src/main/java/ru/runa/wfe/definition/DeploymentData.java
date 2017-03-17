package ru.runa.wfe.definition;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.ForeignKey;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

/**
 * @author Egor Litvinenko
 * @since 13.03.17
 */
@MappedSuperclass
public abstract class DeploymentData extends Identifiable {
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
    private Actor lockActor;
    private Date lockDate;
    private Boolean lockForAll;

    @Transient
    public Long id() {
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

    @ManyToOne
    @JoinColumn(name = "LOCK_USER_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK_DEFINITION_LOCK_USER")
    public Actor getLockActor() {
        return lockActor;
    }

    public void setLockActor(Actor lockActor) {
        this.lockActor = lockActor;
    }

    @Column(name = "LOCK_DATE", nullable = true)
    public Date getLockDate() {
        return lockDate;
    }

    public void setLockDate(Date lockDate) {
        this.lockDate = lockDate;
    }

    @Column(name = "LOCK_FOR_ALL")
    public Boolean getLockForAll() {
        return lockForAll;
    }

    public void setLockForAll(Boolean lockForAll) {
        this.lockForAll = lockForAll;
    }

    @Transient
    @Override
    public Long getIdentifiableId() {
        return Long.valueOf(getName().hashCode());
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
        if (null != obj && DeploymentData.class.isAssignableFrom(obj.getClass())) {
            DeploymentData d = (DeploymentData) obj;
            return Objects.equal(name, d.name) && Objects.equal(version, d.version);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", id).add("name", name).add("version", version).toString();
    }

}
