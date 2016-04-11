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
package ru.runa.af.web;

import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

public class BatchExecutorPermissionHelper {
    private static final SecuredObjectType[] ACTOR_GROUP_CLASSESS = { SecuredObjectType.ACTOR, SecuredObjectType.GROUP };

    public static boolean isAllowedForAnyone(User user, List<? extends Executor> executors, BatchPresentation batchPresentation, Permission permission) {
        List<Executor> executorsWithPermission = Delegates.getAuthorizationService().getPersistentObjects(user, batchPresentation, Executor.class,
                permission, ACTOR_GROUP_CLASSESS, false);
        for (int i = 0; i < executors.size(); i++) {
            if (executorsWithPermission.contains(executors.get(i))) {
                return true;
            }
        }
        return false;
    }
}
