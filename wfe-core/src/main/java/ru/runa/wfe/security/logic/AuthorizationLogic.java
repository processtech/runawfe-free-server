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

import java.util.Collection;
import java.util.List;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.PresentationCompilerHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.PresentationConfiguredCompiler;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.base.Preconditions;

/**
 * Created on 14.03.2005
 * 
 */
public class AuthorizationLogic extends CommonLogic {
    public boolean isAllowed(User user, Permission permission, SecuredObjectType securedObjectType, Long identifiableId) {
        return permissionDAO.isAllowed(user, permission, securedObjectType, identifiableId);
    }

    public <T extends Identifiable> boolean[] isAllowed(User user, Permission permission, List<T> identifiables) {
        return permissionDAO.isAllowed(user, permission, identifiables);
    }

    public boolean isAllowedForAny(User user, Permission permission, SecuredObjectType securedObjectType) {
        return permissionDAO.isAllowedForAny(user, permission, securedObjectType);
    }

    public List<Permission> getIssuedPermissions(User user, Executor performer, Identifiable identifiable) {
        checkPermissionsOnExecutor(user, performer, Permission.READ);
        checkPermissionAllowed(user, identifiable, Permission.READ);
        return permissionDAO.getIssuedPermissions(performer, identifiable);
    }

    public void setPermissions(User user, List<Long> executorIds, Collection<Permission> permissions, Identifiable identifiable) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        for (Executor executor : executors) {
            setPermissions(user, executor, permissions, identifiable);
        }
    }

    public void setPermissions(User user, List<Long> executorIds, List<Collection<Permission>> permissions, Identifiable identifiable) {
        List<Executor> executors = executorDAO.getExecutors(executorIds);
        Preconditions.checkArgument(executors.size() == permissions.size(), "arrays length differs");
        checkPermissionsOnExecutors(user, executors, Permission.READ);
        for (int i = 0; i < executors.size(); i++) {
            setPermissions(user, executors.get(i), permissions.get(i), identifiable);
        }
    }

    public void setPermissions(User user, Long executorId, Collection<Permission> permissions, Identifiable identifiable) {
        Executor executor = executorDAO.getExecutor(executorId);
        setPermissions(user, executor, permissions, identifiable);
    }

    public void setPermissions(User user, Executor executor, Collection<Permission> permissions, Identifiable identifiable) {
        checkPermissionsOnExecutor(user, executor, Permission.READ);
        checkPermissionAllowed(user, identifiable, Permission.UPDATE_PERMISSIONS);
        permissionDAO.setPermissions(executor, permissions, identifiable);
    }

    /**
     * Load executor's which already has (or not has) some permission on specified identifiable. This query using paging.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions.
     * @return Executors with or without permission on {@linkplain Identifiable} .
     */
    public List<? extends Executor> getExecutorsWithPermission(User user, Identifiable identifiable, BatchPresentation batchPresentation,
            boolean hasPermission) {
        checkPermissionAllowed(user, identifiable, Permission.READ);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, identifiable,
                batchPresentation, hasPermission);
        if (hasPermission) {
            List<Executor> executors = compiler.getBatch();
            for (Executor privelegedExecutor : permissionDAO.getPrivilegedExecutors(identifiable.getSecuredObjectType())) {
                if (batchPresentation.getClassPresentation().getPresentationClass().isInstance(privelegedExecutor)
                        && isPermissionAllowed(user, privelegedExecutor, Permission.READ)) {
                    executors.add(0, privelegedExecutor);
                }
            }
            return executors;
        } else {
            return compiler.getBatch();
        }
    }

    /**
     * Load executor's count which already has (or not has) some permission on specified identifiable.
     * 
     * @param user
     *            Current actor {@linkplain User}.
     * @param identifiable
     *            {@linkplain Identifiable} to load executors, which has (or not) permission on this identifiable.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} for loading executors.
     * @param hasPermission
     *            Flag equals true to load executors with permissions on {@linkplain Identifiable}; false to load executors without permissions.
     * @return Count of executors with or without permission on {@linkplain Identifiable}.
     */
    public int getExecutorsWithPermissionCount(User user, Identifiable identifiable, BatchPresentation batchPresentation, boolean hasPermission) {
        checkPermissionAllowed(user, identifiable, Permission.READ);
        PresentationConfiguredCompiler<Executor> compiler = PresentationCompilerHelper.createExecutorWithPermissionCompiler(user, identifiable,
                batchPresentation, hasPermission);
        return compiler.getCount();
    }

}
