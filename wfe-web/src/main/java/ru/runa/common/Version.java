package ru.runa.common;

import ru.runa.wfe.commons.SystemProperties;

public class Version {
    private static String version = SystemProperties.getVersion();
    private static boolean display = WebResources.isVersionDisplay();

    public static boolean isDisplay() {
        return display;
    }

    public static String get() {
        return version;
    }

}
