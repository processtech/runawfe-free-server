package ru.runa.wfe.extension.orgfunction;

import java.util.Set;

import ru.runa.wfe.commons.PropertyResources;

/**
 * Created 19.05.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
class DemoChiefResources {
    private static final PropertyResources RESOURCES = new PropertyResources("demo.chief.properties");

    public static Set<String> getPatterns() {
        return RESOURCES.getAllPropertyNames();
    }

    public static String getChiefName(String pattern) {
        return RESOURCES.getStringPropertyNotNull(pattern);
    }
}
