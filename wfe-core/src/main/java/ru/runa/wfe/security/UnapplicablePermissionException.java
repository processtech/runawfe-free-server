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
package ru.runa.wfe.security;

import com.google.common.collect.Lists;
import java.util.Collection;
import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that {@link java.security.Permission}s are not applicable on selected SecuredObjectType.
 * 
 */
public class UnapplicablePermissionException extends InternalApplicationException {
    private static final long serialVersionUID = 8758756795316935351L;
    private final Collection<Permission> permissions;

    public UnapplicablePermissionException(SecuredObjectType type, Collection<Permission> permissions) {
        super(permissions + " are not applicable for " + type.getName());
        this.permissions = Lists.newArrayList(permissions);
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }
}
