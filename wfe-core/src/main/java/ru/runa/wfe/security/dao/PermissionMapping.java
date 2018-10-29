package ru.runa.wfe.security.dao;

import com.google.common.base.Objects;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

@Entity
@Table(name = "PERMISSION_MAPPING")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
public class PermissionMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PERMISSION_MAPPING", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false)
    private Executor executor;

    @Column(name = "OBJECT_TYPE", nullable = false)
    @Type(type = "ru.runa.wfe.commons.hibernate.SecuredObjectTypeType")
    @QueryType(PropertyType.COMPARABLE)
    private SecuredObjectType objectType;

    @Column(name = "OBJECT_ID", nullable = false)
    private Long objectId;

    @Column(name = "PERMISSION", nullable = false)
    @Type(type = "ru.runa.wfe.commons.hibernate.PermissionType")
    @QueryType(PropertyType.COMPARABLE)
    private Permission permission;

    protected PermissionMapping() {
    }

    public PermissionMapping(Executor executor, SecuredObject securedObject, Permission permission) {
        setExecutor(executor);
        setObjectType(securedObject.getSecuredObjectType());
        setObjectId(securedObject.getIdentifiableId());
        setPermission(permission);
    }

    public PermissionMapping(Executor executor, SecuredObjectType objectType, Long objectId, Permission permission) {
        setExecutor(executor);
        setObjectType(objectType);
        setObjectId(objectId);
        setPermission(permission);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PermissionMapping)) {
            return false;
        }
        PermissionMapping pm = (PermissionMapping) obj;
        return Objects.equal(getExecutor(), pm.getExecutor()) &&
                Objects.equal(objectType, pm.objectType) &&
                Objects.equal(objectId, pm.objectId) &&
                Objects.equal(permission, pm.permission);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getExecutor(), objectType, objectId, permission);
    }
}
