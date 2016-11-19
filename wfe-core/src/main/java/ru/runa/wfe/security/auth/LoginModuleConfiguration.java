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
/*
 * Created on 19.07.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ru.runa.wfe.security.auth;

import java.util.HashMap;
import java.util.List;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * Created on 19.07.2004
 */
public class LoginModuleConfiguration extends Configuration implements InitializingBean {
    public static final String APP_NAME = LoginModuleConfiguration.class.getSimpleName();
    private List<String> loginModuleClassNames;
    private Configuration delegatedConfiguration;

    static {
        // for kerberos
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }

    public LoginModuleConfiguration() {
        delegatedConfiguration = getConfiguration();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setConfiguration(this);
    }

    public static void checkThisIsDefaultConfiguration() {
        if (!(getConfiguration() instanceof LoginModuleConfiguration)) {
            setConfiguration(new LoginModuleConfiguration());
        }
    }

    @Required
    public void setLoginModuleClassNames(List<String> loginModuleClassNames) {
        this.loginModuleClassNames = loginModuleClassNames;
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String applicationName) {
        if (APP_NAME.equals(applicationName)) {
            AppConfigurationEntry[] entries = new AppConfigurationEntry[loginModuleClassNames.size()];
            for (int i = 0; i < entries.length; i++) {
                entries[i] = new AppConfigurationEntry(loginModuleClassNames.get(i), LoginModuleControlFlag.SUFFICIENT, new HashMap<String, Object>());
            }
            return entries;
        }
        if (KerberosLoginModuleResources.isEnabled() && KerberosLoginModuleResources.getApplicationName().equals(applicationName)) {
            AppConfigurationEntry appConfigurationEntry = new AppConfigurationEntry(KerberosLoginModuleResources.getLoginModuleClassName(),
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, KerberosLoginModuleResources.getInitParameters());

            return new AppConfigurationEntry[] { appConfigurationEntry };

        }
        return delegatedConfiguration.getAppConfigurationEntry(applicationName);
    }
}
