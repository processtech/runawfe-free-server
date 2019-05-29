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
