package ru.runa.wfe.service.utils;

import ru.runa.wfe.commons.PropertyResources;

public class ApiProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("api.properties");

    public static int getRetriesCount() {
        return RESOURCES.getIntegerProperty("service.invocation.retries.count", 1);
    }

    public static int getRetryTimeoutMilliseconds() {
        return RESOURCES.getIntegerProperty("service.invocation.retry.timeout.milliseconds", 1000);
    }

    public static boolean suppressExternalExceptions() {
        return RESOURCES.getBooleanProperty("service.invocation.suppress.external.exceptions", false);
    }

}
