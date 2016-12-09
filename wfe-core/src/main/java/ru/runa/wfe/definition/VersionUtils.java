package ru.runa.wfe.definition;

public class VersionUtils {

    public static String increment(String subversion) {
        final long sv = Long.parseLong(subversion.trim());
        return convertVersion(sv + 1);
    }

    public static String incrementSubversion(String subversion) throws MaxSubversionExeption {
        final int sv = Integer.parseInt(subversion);
        if (sv == 99) {
            throw new MaxSubversionExeption("Subversion can not be more than 99");
        }
        return String.format("%02d", sv + 1);
    }

    public static String convertVersion(Long version) {
        return String.format("%5d", version);
    }
}
