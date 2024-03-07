package ru.runa.wfe.user;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.security.SecuredObjectType;

/**
 * Represents a group of {@link Executor}s.
 *
 * Created on 01.07.2004
 */
@Entity
@DiscriminatorValue(value = Group.DISCRIMINATOR_VALUE)
public class Group extends Executor {
    private static final long serialVersionUID = -4353040407820259331L;
    public static final String DISCRIMINATOR_VALUE = "Y";
    public static final Group UNAUTHORIZED_GROUP = new Group(UNAUTHORIZED_EXECUTOR_NAME, null);

    private String ldapGroupName;

    protected Group() {
    }

    /**
     * Creates an {@link Group}
     *
     * @param name
     *            {@link Group}name
     * @param description
     *            {@link Group}description. If description is null, constructor changes it to empty String value
     * @throws NullPointerException
     *             if {@link Group}name is null
     */
    public Group(String name, String description) {
        this(name, description, null);
    }

    public Group(String name, String description, String activeDirectoryGroup) {
        super(name, description, name);
        this.ldapGroupName = activeDirectoryGroup;
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.EXECUTOR;
    }

    @Column(name = "E_MAIL")
    public String getLdapGroupName() {
        return ldapGroupName;
    }

    public void setLdapGroupName(String activeDirectoryGroup) {
        this.ldapGroupName = activeDirectoryGroup;
    }

    @Transient
    public boolean isTemporary() {
        return false;
    }

    protected void updateFullName() {
        setFullName(getName());
    }
}
