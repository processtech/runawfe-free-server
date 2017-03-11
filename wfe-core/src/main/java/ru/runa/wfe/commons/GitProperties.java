package ru.runa.wfe.commons;

public class GitProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("git.properties", false, false);

    /**
     * Get branch from which system built
     */
    public static String getBranch() {
        return RESOURCES.getStringProperty("git.branch");
    }

    /**
     * Get commit from which system built
     */
    public static String getCommit() {
        return RESOURCES.getStringProperty("git.commit.id.describe");
    }

}
