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
import java.security.Principal;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class PrincipalCallbackHandler implements CallbackHandler {

    private final Principal principal;

    public PrincipalCallbackHandler(Principal principal) {
        this.principal = principal;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof PrincipalCallback) {
                PrincipalCallback callback = (PrincipalCallback) callbacks[i];
                callback.setPrincipal(principal);
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback"
                        + ((callbacks[i] != null) ? " of type " + callbacks[i].getClass().getName() + " value " + callbacks[i].toString() : ""));
            }
        }
    }
}
