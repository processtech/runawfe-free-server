package ru.runa.wfe.presentation;

/**
 * Filter mode, available for {@link BatchPresentation} field.
 */
public enum FieldFilterMode {

    /**
     * Field is not support filtering.
     */
    NONE,

    /**
     * Field will be filtered in database. HQL/SQL query will be constructed with filter restrictions.
     */
    DATABASE,

    /**
     * Field will be filtered in database, but entity table must not be joined, just used to select root entity id's. HQL/SQL query will be
     * constructed with filter restrictions. No sort restrictions is available.
     */
    DATABASE_ID_RESTRICTION,

    /**
     * Filed is filtered in application code.
     */
    APPLICATION;
}
