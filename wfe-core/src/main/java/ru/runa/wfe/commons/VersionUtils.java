package ru.runa.wfe.commons;

import org.apache.commons.lang.StringUtils;

public class VersionUtils {

    public static String increment(String subversion) {
        final int sv = Integer.parseInt(subversion);
        return Integer.toString(sv + 1);
    }

    public static String incrementSubversion(String subversion) {
        return StringUtils.leftPad(increment(subversion), 2, '0');
    }
}
