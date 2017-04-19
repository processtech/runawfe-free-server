package ru.runa.common;

import ru.runa.wfe.commons.GitProperties;
import ru.runa.wfe.commons.SystemProperties;

import com.google.common.base.Joiner;

public class Version {
    private static final String version = SystemProperties.getVersion();
    private static final String buildInfo;

    static {
        buildInfo = Joiner.on(", ").skipNulls().join(GitProperties.getBranch(), GitProperties.getCommit(), SystemProperties.getBuildDateString());
    }

    public static String get() {
        return version;
    }

    public static String getBuildInfo() {
        return buildInfo;
    }
    
    public static String getHash(){
    	return GitProperties.getCommit();
    }
}
