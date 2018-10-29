package ru.runa.wfe.presentation;

import ru.runa.wfe.commons.PropertyResources;

public class ClassPresentationResources {
    private static final PropertyResources RESOURCES = new PropertyResources("class.presentation.properties", false);

    public static FieldState getFieldState(String property) {
        if (property.startsWith("batch_presentation.")) {
            property = property.substring(19);
        }
        if (property.startsWith(ClassPresentation.editable_prefix + "name:batch_presentation.")) {
            property = property.substring((ClassPresentation.editable_prefix + "name:batch_presentation.").length());
        }
        String value = RESOURCES.getStringProperty(property);
        if (value == null) {
            return FieldState.ENABLED;
        }
        if (value.equalsIgnoreCase("ENABLED")) {
            return FieldState.ENABLED;
        }
        if (value.equalsIgnoreCase("DISABLED")) {
            return FieldState.DISABLED;
        }
        if (value.equalsIgnoreCase("HIDDEN")) {
            return FieldState.HIDDEN;
        }
        throw new IllegalArgumentException("Property " + property + " must be enabled, hidden or disabled; but found " + value);
    }
}
