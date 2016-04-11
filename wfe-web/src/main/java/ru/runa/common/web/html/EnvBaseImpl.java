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
package ru.runa.common.web.html;

import java.util.HashMap;
import java.util.Map;

import ru.runa.common.web.Commons;
import ru.runa.common.web.html.TDBuilder.Env;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public abstract class EnvBaseImpl implements Env {
    private User user;

    @Override
    public User getUser() {
        if (user == null) {
            user = Commons.getUser(getPageContext().getSession());
        }
        return user;
    }

    @Override
    public boolean hasProcessDefinitionPermission(Permission permission, Long processDefinitionId) {
        try {
            Boolean result = processDefPermissionCache.get(processDefinitionId);
            if (result != null) {
                return result;
            }
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getUser(), processDefinitionId);
            result = Delegates.getAuthorizationService().isAllowed(getUser(), permission, definition);
            processDefPermissionCache.put(processDefinitionId, result);
            return result;
        } catch (AuthorizationException e) {
            processDefPermissionCache.put(processDefinitionId, false);
            return false;
        }
    }

    private final Map<Long, Boolean> processDefPermissionCache = new HashMap<Long, Boolean>();
}
