package ru.runa.wfe.var.format;

import ru.runa.wfe.var.UserType;

/**
 * Container marker. Used for lists, maps, etc.
 *
 * @author dofs
 * @since 4.0.5
 */
public interface VariableFormatContainer {
    String SIZE_SUFFIX = ".size";
    String MAP_KEY_SUFFIX = ":k";
    String MAP_VALUE_SUFFIX = ":v";
    String COMPONENT_QUALIFIER_START = "[";
    String COMPONENT_QUALIFIER_END = "]";
    String COMPONENT_PARAMETERS_START = "(";
    String COMPONENT_PARAMETERS_END = ")";
    String COMPONENT_PARAMETERS_DELIM = ", ";

    /**
     * @return component format by index.
     */
    String getComponentClassName(int index);

    /**
     * Sets component formats.
     */
    void setComponentClassNames(String[] componentClassNames);

    /**
     * @return user types by index
     */
    UserType getComponentUserType(int index);

    void setComponentUserTypes(UserType[] componentUserTypes);

}
