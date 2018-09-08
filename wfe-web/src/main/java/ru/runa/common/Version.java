package ru.runa.common;

import com.google.common.base.Joiner;
import java.util.Calendar;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.GitProperties;
import ru.runa.wfe.commons.SystemProperties;

public class Version {
    private static final String version = SystemProperties.getVersion();
    private static final String buildInfo;
    private static final Calendar SYSTEM_STARTUP_CALENDAR = Calendar.getInstance();

    static {
        buildInfo = Joiner.on(", ").skipNulls().join(GitProperties.getBranch(), GitProperties.getCommit(), SystemProperties.getBuildDateString());
    }

    public static void init() {
    }

    public static String get() {
        return version;
    }

    public static String getBuildInfo() {
        return buildInfo;
    }

    public static String getStartupDateTimeString() {
        return CalendarUtil.formatDateTime(SYSTEM_STARTUP_CALENDAR);
    }

    public static String getHash() {
        return GitProperties.getCommit();
    }
}
