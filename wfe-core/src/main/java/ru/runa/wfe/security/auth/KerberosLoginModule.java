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
            return executorDao.getActorCaseInsensitive(actorName);
        }
        return null;
    }

}
