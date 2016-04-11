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
package ru.runa.wfe.security.auth;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

/**
 * Trusted authentication based on service user and login using internal
 * database.
 * 
 * @since 4.2.0
 */
public class TrustedLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        if (callbackHandler instanceof TrustedLoginModuleCallbackHandler) {
            if (!SystemProperties.isTrustedAuthenticationEnabled()) {
                log.warn("trusted auth is disabled in system.properties");
                return null;
            }
            TrustedLoginModuleCallbackHandler handler = (TrustedLoginModuleCallbackHandler) callbackHandler;
            Group administratorsGroup = executorDAO.getGroup(SystemProperties.getAdministratorsGroupName());
            if (!executorDAO.isExecutorInGroup(handler.getServiceUser().getActor(), administratorsGroup)) {
                throw new LoginException("Service user does not belongs to administrators");
            }
            return executorDAO.getActor(handler.getLogin());
        }
        return null;
    }

}
