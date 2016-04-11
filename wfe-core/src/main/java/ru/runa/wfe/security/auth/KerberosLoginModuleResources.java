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

import java.util.Map;

import ru.runa.wfe.commons.PropertyResources;

/**
 * Created on 11.01.2006
 * 
 * @author Gritsenko_S
 */
public class KerberosLoginModuleResources {
    private static final PropertyResources RESOURCES = new PropertyResources("kerberos.properties", false);

    public static boolean isEnabled() {
        return isApiAuthEnabled() || isHttpAuthEnabled();
    }

    public static boolean isApiAuthEnabled() {
        return RESOURCES.getBooleanProperty("api.auth.enabled", false);
    }

    public static boolean isHttpAuthEnabled() {
        return RESOURCES.getBooleanProperty("http.auth.enabled", false);
    }

    public static String getApplicationName() {
        return RESOURCES.getStringPropertyNotNull("appName");
    }

    public static String getLoginModuleClassName() {
        return RESOURCES.getStringPropertyNotNull("moduleClassName");
    }

    public static Map<String, String> getInitParameters() {
        return RESOURCES.getAllProperties();
    }

    public static String getServerPrincipal() {
        return RESOURCES.getStringPropertyNotNull("principal");
    }

}
