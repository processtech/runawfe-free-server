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

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import ru.runa.wfe.user.Actor;

/**
 * Created on 2006
 * 
 */
public class KerberosLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        if (callbackHandler instanceof KerberosCallbackHandler) {
            if (!KerberosLoginModuleResources.isEnabled()) {
                log.warn("kerberos auth is disabled in kerberos.properties");
                return null;
            }
            KerberosCallback kerberosCallback = new KerberosCallback();
            callbackHandler.handle(new Callback[] { kerberosCallback });

            GSSManager manager = GSSManager.getInstance();
            GSSName serverName = manager.createName(KerberosLoginModuleResources.getServerPrincipal(), null);
            GSSCredential credential = manager.createCredential(serverName, GSSCredential.INDEFINITE_LIFETIME, (Oid) null, GSSCredential.ACCEPT_ONLY);
            GSSContext context = manager.createContext(credential);
            context.requestMutualAuth(false);

            byte[] authToken = kerberosCallback.getAuthToken();
            context.acceptSecContext(authToken, 0, authToken.length);

            String domainActorName = context.getSrcName().toString();
            String actorName = domainActorName.substring(0, domainActorName.indexOf("@"));
            if (actorName == null) {
                throw new LoginException("No client name was provided.");
            }
            return executorDAO.getActorCaseInsensitive(actorName);
        }
        return null;
    }

}
