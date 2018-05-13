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
package ru.runa.wfe.security.logic;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.List;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Created on 14.03.2005
 */
public class AuthorizationLogic extends CommonLogic {
    public boolean isAllowed(User user, Permission permission, SecuredObjectType securedObjectType, Long identifiableId) {
        return permissionDAO.isAllowed(user, permission, securedObjectType, identifiableId);
    }

    public <T extends SecuredObject> boolean[] isAllowed(User user, Permission permission, List<T> securedObjects) {
        return permissionDAO.isAllowed(user, permission, securedObjects);
    }

    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectType) {
        return permissionDAO.isAllowedForAny(user, permission, securedObjectType);
    }

    public List<Permission> getIssuedPermissions(User user, Executor performer, SecuredObject securedObject) {
        checkPermissionsOnExecutor(user, performer, Permission.LIST);
        permissionDAO.checkAllowed(user, Permission.LIST, securedObject);
        return permissionDAO.getIssuedPermissions(performer, securedObject);
    }

    public void setPermissions(User user, List<Long> executorIds, Collection<Permission> permissions, SecuredObject securedObject) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkPermissionsOnExecutors(user, executors, Permission.LIST);
        for (Executor executor : executors) {
            setPermissions(user, executor, permissions, securedObject);
        }
    }

    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, SecuredObject securedObject) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Preconditions.checkArgument(executors.size() == permissions.size(), "arrays length differs");
        checkPermissionsOnExecutors(user, executors, Permission.LIST);
        for (int i = 0; i < executors.size(); i++) {
            setPermissions(user, executors.get(i), permissions.get(i), securedObject);
        }
    }

    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, SecuredObject securedObject) {
        Executor executor = executorDAO.getExecutor(executorId);
        setPermissions(user, executor, permissions, securedObject);
    }

    public void setPermissions(User user, Executor executor, Collection<Permission> permissions, SecuredObject securedObject) {
        checkPermissionsOnExecutor(user, executor, Permission.LIST);
        permissionDAO.checkAllowed(user, Permission.UPDATE_PERMISSIONS, securedObject);
        permissionDAO.setPermissions(executor, permissions, securedObject);
    }

    /**
     * Load executor's which already has (or not has) some permission on specified securedObject. This query using paging.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain SecuredObject}; false to load executors without permissions.
     * @return Executors with or without permission on {@linkplain SecuredObject} .
     */
    public List<? extends Executor> getExecutorsWithPermission(User user, SecuredObject securedObject, BatchPresentation batchPresentation,
            boolean hasPermission) {
        permissionDAO.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, securedObject,
                batchPresentation, hasPermission);
        if (hasPermission) {
            List<Executor> executors = compiler.getBatch();
            for (Executor privelegedExecutor : permissionDAO.getPrivilegedExecutors(securedObject.getSecuredObjectType())) {
                if (batchPresentation.getClassPresentation().getPresentationClass().isInstance(privelegedExecutor)
                        && permissionDAO.isAllowed(user, Permission.LIST, privelegedExecutor)) {
                    executors.add(0, privelegedExecutor);
                }
            }
            return executors;
        } else {
            return compiler.getBatch();
        }
    }

    /**
     * Load executor's count which already has (or not has) some permission on specified securedObject.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param securedObject
     *            {@linkplain SecuredObject} to load executors, which has (or not) permission on this securedObject.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain SecuredObject}; false to load executors without permissions.
     * @return Count of executors with or without permission on {@linkplain SecuredObject}.
     */
    public int getExecutorsWithPermissionCount(User user, SecuredObject securedObject, BatchPresentation batchPresentation, boolean hasPermission) {
        permissionDAO.checkAllowed(user, Permission.READ_PERMISSIONS, securedObject);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, securedObject,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

}
