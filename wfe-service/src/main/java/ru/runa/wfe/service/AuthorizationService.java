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
package ru.runa.wfe.service;

import java.util.Collection;
import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Service for authorization.
 * 
 * @since 2.0
 */
public interface AuthorizationService {

    /**
     * Checks whether user has permission on identifiable.
     */
    public boolean isAllowed(User user, Permission permission, Identifiable identifiable);

    /**
     * Checks whether user has permission on object.
     */
    public boolean isAllowed(User user, Permission permission, SecuredObjectType securedObjectType, Long identifiableId);

    /**
     * Checks whether user has permission on identifiables of the same secured
     * object type.
     */
    public <T extends Identifiable> boolean[] isAllowed(User user, Permission permission, List<T> identifiables);

    /**
     * Checks if user has parmission on any object of specified type.
     */
    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectType);

    /**
     * Sets permissions for executor specified by id on identifiable.
     */
    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, Identifiable identifiable);

    /**
     * Sets permissions for executors specified by ids on identifiable.
     */
    public void setPermissions(User user, List<Long> executorsId, List<Collection<Permission>> permissions, Identifiable identifiable);

    /**
     * Sets permissions for executors specified by ids on identifiable.
     */
    public void setPermissions(User user, List<Long> executorsId, Collection<Permission> permissions, Identifiable identifiable);

    /**
     * Returns permissions that executor himself has on identifiable.
     * Permissions by privilege will not return.
     * 
     * @return Map of {Permission, Is permission can be modifiable}, not <code>null</code>
     */
    public List<Permission> getIssuedPermissions(User user, Executor performer, Identifiable identifiable);

    /**
     * Load executor's which already has (or not has) some permission on
     * specified identifiable. This query using paging.
     * 
     * @param user
     *            Current user {@linkplain User}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or
     *            not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain Identifiable}; false to load executors without
     *            permissions.
     * @return Executors with or without permission on {@linkplain Identifiable}
     */
    public List<Executor> getExecutorsWithPermission(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission);

    /**
     * Load executor's count which already has (or not has) some permission on
     * specified identifiable.
     * 
     * @param user
     *            Current user {@linkplain User}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or
     *            not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on
     *            {@linkplain Identifiable}; false to load executors without
     *            permissions.
     * @return Count of executors with or without permission on
     *         {@linkplain Identifiable}.
     */
    public int getExecutorsWithPermissionCount(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission);

    /**
     * Loads identifiables with permission filtering.
     */
    public <T extends Object> List<T> getPersistentObjects(User user, BatchPresentation batchPresentation, Class<T> persistentClass,
                                                           Permission permission, SecuredObjectType[] securedObjectTypes, boolean enablePaging);

}
