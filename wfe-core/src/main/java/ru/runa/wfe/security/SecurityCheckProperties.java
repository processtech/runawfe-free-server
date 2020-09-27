package ru.runa.wfe.security;

import ru.runa.wfe.commons.PropertyResources;

public class SecurityCheckProperties {
    public static final String CONFIG_FILE_NAME = "securitycheck.properties";
    private static final PropertyResources RESOURCES = new PropertyResources(CONFIG_FILE_NAME);
    
    public static boolean isPermissionCheckRequired(SecuredObjectType securedObjectType) {
        return RESOURCES.getBooleanProperty("permission.check.required." + securedObjectType.getName(), false);
    }    
}
