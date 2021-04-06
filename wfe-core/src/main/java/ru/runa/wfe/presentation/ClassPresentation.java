package ru.runa.wfe.presentation;

/**
 * Presentation class, contains information about persistent class and object properties, what can be used in batch presentation.
 */
public class ClassPresentation {

    /**
     * At DB request HQL/SQL string requested (root persistent) class has this name.
     */
    public static final String classNameSQL = "instance";
    /**
     * Edited fields format: editable_prefix:DB path to property value:displayed name. If field has this prefix, when it must be showed as editor
     * and not affecting HQL/SQL queries. After insert value into editor, batch presentation must store this value and create removable field, which
     * will affect HQL/SQL queries.
     */
    public static final String editable_prefix = "editable:";

    /**
     * Removable field format: removable_prefix:value. Value in field format is a text, inserted by user to editor, created for editable field.
     */
    public static final String removable_prefix = "removable:";

    /**
     * Filtered fields format:filterable_prefix:displayed name. If field has this prefix, when it must be showed as check box and it use for
     * grouping.
     */
    public static final String filterable_prefix = "filterable:";

    /**
     * Root persistent class of {@link ClassPresentation}. All fields is queried against this object.
     */
    private final Class<?> clazz;

    /**
     * Predefined restrictions for root persistent object. For example {@link BatchPresentation} must returns only objects, with some property set:
     * classNameSQL + ".property is not null".
     */
    private final String classRestrictions;

    /**
     * Fields (properties), available via {@link BatchPresentation}. WFE will support filter/sort only by this fields.
     */
    private final FieldDescriptor[] fields;

    /**
     * Flag, equals true, if paging is enabled for persistent class loading; false otherwise.
     */
    private final boolean withPaging;

    /**
     * Creates class presentation instance.
     *
     * @param clazz
     *            Root persistent class.
     * @param classRestrictions
     *            Predefined restrictions for root persistent object.
     * @param withPaging
     *            Flag, equals true, if paging is enabled for persistent class loading; false otherwise.
     * @param fields
     *            Fields (properties), available via {@link BatchPresentation}.
     */
    public ClassPresentation(Class<?> clazz, String classRestrictions, boolean withPaging, FieldDescriptor[] fields) {
        this.clazz = clazz;
        this.classRestrictions = classRestrictions;
        this.fields = fields;
        this.withPaging = withPaging;
    }

    /**
     * Root persistent class.
     */
    public Class<?> getPresentationClass() {
        return clazz;
    }

    /**
     * Predefined restrictions for root persistent object.
     */
    public String getRestrictions() {
        return classRestrictions;
    }

    /**
     * Fields (properties), available via {@link BatchPresentation}.
     */
    public FieldDescriptor[] getFields() {
        return fields;
    }

    /**
     * @return Flag, equals true, if paging is enabled for persistent class loading; false otherwise.
     */
    public boolean isWithPaging() {
        return withPaging;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }
}
