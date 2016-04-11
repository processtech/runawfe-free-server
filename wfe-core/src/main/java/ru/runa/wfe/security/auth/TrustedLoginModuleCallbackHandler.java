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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import ru.runa.wfe.user.User;

/**
 * Callback handler for TrustedLoginModule.
 * 
 * @since 4.2.0
 */
public class TrustedLoginModuleCallbackHandler implements CallbackHandler {
    private final User serviceUser;
    private final String login;

    public TrustedLoginModuleCallbackHandler(User serviceUser, String login) {
        this.serviceUser = serviceUser;
        this.login = login;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    }

    public User getServiceUser() {
        return serviceUser;
    }

    public String getLogin() {
        return login;
    }
}
