package ru.runa.wfe.office;

import ru.runa.wfe.commons.PropertyResources;

public class OfficeProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("office.properties", false);

    public static String getDocxPlaceholderStart() {
        return RESOURCES.getStringProperty("docx.placeholder.start", "${");
    }

    public static String getDocxPlaceholderEnd() {
        return RESOURCES.getStringProperty("docx.placeholder.end", "}");
    }

    public static String getDocxPlaceholderVMerge() {
        return RESOURCES.getStringProperty("docx.placeholder.vmerge", "-||-");
    }

}
