package ru.runa.wfe.task;

import ru.runa.wfe.commons.PropertyResources;

public class TaskProperties {
    public static final String CONFIG_FILE_NAME = "task.properties";
    private static final PropertyResources RESOURCES = new PropertyResources(CONFIG_FILE_NAME);
    
    public static boolean isTaskDelegationEnabled() {
        return RESOURCES.getBooleanProperty("task.delegation.enabled", true);
    }    
}
