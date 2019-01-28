/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
@DiscriminatorValue(value = "Y")
public class Group extends Executor {
    private static final long serialVersionUID = -4353040407820259331L;

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
        super(name, description);
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
}
