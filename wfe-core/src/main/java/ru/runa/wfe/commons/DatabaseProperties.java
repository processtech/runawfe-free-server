package ru.runa.wfe.commons;

public class DatabaseProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("database.properties", true, false);

    public static boolean isDatabaseSettingsEnabled() {
    	return RESOURCES.getBooleanProperty("database.settings.enabled", true);
    }

}
