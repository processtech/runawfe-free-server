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

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecurityCheckProperties;
import ru.runa.wfe.security.auth.InternalDbNameLoginModule;
import ru.runa.wfe.security.auth.KerberosCallbackHandler;
import ru.runa.wfe.security.auth.LoginModuleConfiguration;
import ru.runa.wfe.security.auth.PasswordLoginModuleCallbackHandler;
import ru.runa.wfe.security.auth.PrincipalCallbackHandler;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.security.auth.TrustedLoginModuleCallbackHandler;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * Created on 14.03.2005
 */
public class AuthenticationLogic extends CommonLogic {
    
    private List<LoginHandler> loginHandlers;

    @Required
    public void setLoginHandlers(List<LoginHandler> loginHandlers) {
        this.loginHandlers = loginHandlers;
    }

    public User authenticate(Principal principal) throws AuthenticationException {
        return authenticate(new PrincipalCallbackHandler(principal), AuthType.OTHER);
    }

    public User authenticate(byte[] kerberosToken) throws AuthenticationException {
        return authenticate(new KerberosCallbackHandler(kerberosToken), AuthType.KERBEROS);
    }

    public User authenticate(String name, String password) throws AuthenticationException {
        if (!SecurityCheckProperties.isPermissionCheckRequired(SecuredObjectType.SYSTEM)) {
            Actor curActor = this.executorDao.getActor(name);
            if (curActor != null) {
                if (this.executorDao.hasPassword(curActor)) {
                    return authenticate(new PasswordLoginModuleCallbackHandler(name, password), AuthType.DB);
                } else {
                    return authenticateByName(new PasswordLoginModuleCallbackHandler(name, password), AuthType.DB);
                }
            }
            return authenticateByName(new PasswordLoginModuleCallbackHandler(name, password), AuthType.DB);
        }
        return authenticate(new PasswordLoginModuleCallbackHandler(name, password), AuthType.DB);
    }

    public User authenticate(User serviceUser, String login) throws AuthenticationException {
        return authenticate(new TrustedLoginModuleCallbackHandler(serviceUser, login), AuthType.TRUSTED);
    }

    private User authenticate(CallbackHandler callbackHandler, AuthType authType) throws AuthenticationException {
        try {
            LoginContext loginContext = new LoginContext(LoginModuleConfiguration.APP_NAME, null, callbackHandler, Configuration.getConfiguration());
            loginContext.login();
            Subject subject = loginContext.getSubject();
            User user = SubjectPrincipalsHelper.getUser(subject);
            SubjectPrincipalsHelper.validateUser(user);
            callHandlers(user.getActor(), authType);
            log.debug(user.getName() + " successfully authenticated");
            return user;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }
    
    private User authenticateByName(CallbackHandler callbackHandler, AuthType authType) throws AuthenticationException {
        try {
            ru.runa.wfe.security.auth.LoginModuleConfiguration config = new ru.runa.wfe.security.auth.LoginModuleConfiguration();
            config.setLoginModuleClassNames(Collections.singletonList(InternalDbNameLoginModule.class.getName()));
            
            LoginContext loginContext = new LoginContext(LoginModuleConfiguration.APP_NAME, null, callbackHandler, config);
            loginContext.login();
            Subject subject = loginContext.getSubject();
            User user = SubjectPrincipalsHelper.getUser(subject);
            SubjectPrincipalsHelper.validateUser(user);
            callHandlers(user.getActor(), authType);
            log.debug(user.getName() + " successfully authenticated");
            return user;
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }
    }    

    private void callHandlers(Actor actor, AuthType type) {
        for (LoginHandler handler : loginHandlers) {
            try {
                handler.onUserLogin(actor, type);
            } catch (Throwable e) {
                log.warn("Exception while calling loginHandler " + handler, e);
            }
        }
    }

}
