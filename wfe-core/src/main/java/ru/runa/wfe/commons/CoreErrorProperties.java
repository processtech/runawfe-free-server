package ru.runa.wfe.commons;

import java.util.Properties;

public class CoreErrorProperties {
    public static final String BOT_STATION_CONFIGURATION_ERROR = "error.botstation.configuration";
    public static final String BOT_CONFIGURATION_ERROR = "error.bot.configuration";
    public static final String BOT_TASK_CONFIGURATION_ERROR = "error.bottask.configuration";
    public static final String BOT_TASK_MISSED = "error.bottask.missed";
    private static final Properties properties = ClassLoaderUtil.getLocalizedProperties("core.error", CoreErrorProperties.class);

    public static String getMessage(String pattern, Object... arguments) {
        String message = properties.getProperty(pattern);
        if (arguments != null) {
            return String.format(message, arguments);
        }
        return message;
    }

}
