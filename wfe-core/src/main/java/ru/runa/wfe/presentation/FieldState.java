package ru.runa.wfe.presentation;

/**
 * Field display and HQL/SQL affecting state.
 */
public enum FieldState {

    /**
     * Field will be shown in web interface and affects HQL/SQL queries (by sorting/filtering and so on). 
     */
    ENABLED,

    /**
     * Field will not be shown in web interface and not affects HQL/SQL, even if filter/sort mode installed. 
     */
    DISABLED,

    /**
     * Field will not be shown in web interface but it will be affects HQL/SQL, (by sorting/filtering and so on).
     */
    HIDDEN
}
