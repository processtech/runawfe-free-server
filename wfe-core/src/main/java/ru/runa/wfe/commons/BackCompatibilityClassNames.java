package ru.runa.wfe.commons;

/**
 * This class contains substitution for loaded classes (back compatibility on class loading).
 * 
 * @author dofs
 * @since 4.0
 */
public class BackCompatibilityClassNames {
    private static final PropertyResources RESOURCES = new PropertyResources("back.compatibility.properties", true, false);

    /**
     * Gets back-compatible class name if found.
     * 
     * @param className
     *            original class name
     * @return adjusted class name or original class name
     */
    public static String getClassName(String className) {
        if (className == null) {
            return null;
        }
        String newClassName = RESOURCES.getStringProperty(className);
        if (newClassName != null) {
            return newClassName;
        }
        return className;
    }

}
