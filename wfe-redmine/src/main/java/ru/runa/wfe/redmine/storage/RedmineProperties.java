package ru.runa.wfe.redmine.storage;

import com.google.common.base.Strings;
import ru.runa.wfe.commons.PropertyResources;

public final class RedmineProperties {

    public static final String CONFIG_FILE_NAME = "redmine.properties";

    static final String PROPERTY_URL = "redmine.url";
    static final String PROPERTY_API_KEY = "redmine.apiKey";

    static final String ENV_URL = "REDMINE_URL";
    static final String ENV_API_KEY = "REDMINE_API_KEY";

    private static final PropertyResources RESOURCES = new PropertyResources(CONFIG_FILE_NAME, false);

    private RedmineProperties() {
    }

    public static String getRedmineUrl() {
        return resolve(ENV_URL, PROPERTY_URL);
    }

    public static String getRedmineApiKey() {
        return resolve(ENV_API_KEY, PROPERTY_API_KEY);
    }

    private static String resolve(String envName, String propertyName) {
        String envValue = System.getenv(envName);
        if (!Strings.isNullOrEmpty(envValue)) {
            return envValue;
        }
        return RESOURCES.getStringProperty(propertyName);
    }
}
