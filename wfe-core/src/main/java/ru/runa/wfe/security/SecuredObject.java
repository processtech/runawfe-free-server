package ru.runa.wfe.security;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlType;

/**
 * Interface for all secured object components, which can be secured using
 * permission.
 */
@XmlType
public abstract class SecuredObject implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Return identity for current object. Object with same id and type has same
     * permissions, even if object is not equals. Object with same type but
     * different id has different permissions.
     * 
     * @return Object identity
     */
    public abstract Long getSecuredObjectId();

    /**
     * Returns object type identity. Object with same id and type has same
     * permissions, even if object is not equals. Object with same id but
     * different type has different permissions.
     * 
     * @return Object type identity.
     */
    public abstract SecuredObjectType getSecuredObjectType();

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", getSecuredObjectType()).add("id", getSecuredObjectId()).toString();
    }

}
