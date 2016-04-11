package ru.runa.wfe.var.format;

import ru.runa.wfe.var.UserType;

/**
 * Container marker. Used for lists, maps, etc.
 *
 * @author dofs
 * @since 4.0.5
 */
public interface VariableFormatContainer {
    public static final String SIZE_SUFFIX = ".size";
    public static final String COMPONENT_QUALIFIER_START = "[";
    public static final String COMPONENT_QUALIFIER_END = "]";
    public static final String COMPONENT_PARAMETERS_START = "(";
    public static final String COMPONENT_PARAMETERS_END = ")";
    public static final String COMPONENT_PARAMETERS_DELIM = ", ";

    /**
     * @return component format by index.
     */
    public String getComponentClassName(int index);

    /**
     * Sets component formats.
     */
    public void setComponentClassNames(String[] componentClassNames);

    /**
     * @return user types by index
     */
    public UserType getComponentUserType(int index);

    public void setComponentUserTypes(UserType[] componentUserTypes);

}
