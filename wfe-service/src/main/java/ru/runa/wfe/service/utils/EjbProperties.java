package ru.runa.wfe.service.utils;

import ru.runa.wfe.commons.PropertyResources;

public class EjbProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("ejb.properties");

    public static String getConnectionType() {
        return RESOURCES.getStringPropertyNotNull("ejb.type");
    }

    public static boolean useJbossEjbClientForRemoting() {
        return RESOURCES.getBooleanProperty("jboss.ejbclient.enabled", false);
    }

    public static String getJbossEjbClientJndiNameFormat() {
        return RESOURCES.getStringPropertyNotNull("jboss.ejbclient.url.format");
    }

    public static boolean isJbossEjbClientStaticEnabled() {
        return RESOURCES.getBooleanProperty("jboss.ejbclient.static.enabled", false);
    }

    public static String getJbossEjbClientPort() {
        return RESOURCES.getStringProperty("jboss.ejbclient.port", "4447");
    }

    public static String getJbossEjbClientUsername() {
        return RESOURCES.getStringProperty("jboss.ejbclient.username");
    }

    public static String getJbossEjbClientPassword() {
        return RESOURCES.getStringProperty("jboss.ejbclient.password");
    }

    public static String getJndiNameFormat() {
        return RESOURCES.getStringPropertyNotNull("ejb.jndiName.format");
    }
}
