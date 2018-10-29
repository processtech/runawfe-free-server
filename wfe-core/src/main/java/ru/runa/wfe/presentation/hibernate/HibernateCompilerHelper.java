package ru.runa.wfe.presentation.hibernate;

import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.SingleTableEntityPersister;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;

/**
 * Helper class with some functions, required to compile query for batch presentation.
 */
public final class HibernateCompilerHelper {

    /**
     * Check, if field must affects SQL query.
     *
     * @param field
     *            Filed to check.
     * @param batchPresentation
     *            {@link BatchPresentation}, used to build query.
     * @return True, is field will affects SQL; false otherwise.
     */
    public static boolean isFieldSQLAffects(FieldDescriptor field, BatchPresentation batchPresentation) {
        if (field.fieldState == FieldState.DISABLED) {
            return false;
        }
        FieldDescriptor[] allFields = batchPresentation.getAllFields();
        int idx = 0;
        for (; idx < allFields.length; ++idx) {
            if (allFields[idx].equals(field)) {
                break;
            }
        }
        return (batchPresentation.isFieldFiltered(idx) && field.filterMode == FieldFilterMode.DATABASE) ||
                (
                        (batchPresentation.isSortingField(idx) || batchPresentation.isFieldGroupped(idx)) &&
                        field.sortable &&
                        (!field.displayName.startsWith(ClassPresentation.filterable_prefix) || batchPresentation.isFieldGroupped(idx))
                );
    }

    /**
     * Parse identifier from string.
     *
     * @param sqlRequest
     *            String to parse identifier from.
     * @param tableName
     *            Table name to search identifier.
     * @param forwardSearch
     *            true, to search forward and false otherwise.
     * @return Parsed identifier.
     */
    public static String getIdentifier(StringBuilder sqlRequest, String tableName, boolean forwardSearch) {
        int fromIndex = getFromClauseIndex(sqlRequest);
        return getIdentifier(sqlRequest, sqlRequest.indexOf(" ", sqlRequest.indexOf(tableName, fromIndex)), forwardSearch);
    }

    /**
     * Returns string index, where from clause begins.
     *
     * @param queryString
     *            Query string.
     * @return Returns string index, where from clause begins.
     */
    public static int getFromClauseIndex(StringBuilder queryString) {
        int fromIndex = queryString.indexOf(" from ");
        if (-1 == fromIndex) {
            fromIndex = queryString.indexOf(" FROM ");
        }
        return fromIndex;
    }

    /**
     * Parse identifier from string.
     *
     * @param string
     *            String to parse identifier from.
     * @param idx
     *            Start index, to search identifier.
     * @param forwardSearch
     *            true, to search forward and false otherwise.
     * @return Parsed identifier.
     */
    public static String getIdentifier(CharSequence string, int idx, boolean forwardSearch) {
        while (Character.isWhitespace(string.charAt(idx))) {
            idx = forwardSearch ? idx + 1 : idx - 1;
        }
        int idx1 = idx;
        while (true) {
            char character = string.charAt(idx);
            if (!(Character.isLetter(character) || character == '_' || Character.isDigit(character))) {
                break;
            }
            idx = forwardSearch ? idx + 1 : idx - 1;
        }
        return forwardSearch ? string.subSequence(idx1, idx).toString() : string.subSequence(idx + 1, idx1 + 1).toString();
    }

    /**
     * Get table name from Entity class.
     *
     * @param entityClass
     *            Class to parse table name from.
     * @return Parsed table name.
     */
    public static String getTableName(Class<?> entityClass) {
        final ClassMetadata classMetadata = ApplicationContextFactory.getSessionFactory().getClassMetadata(entityClass);
        if (!(classMetadata instanceof SingleTableEntityPersister)) {
            throw new InternalApplicationException("ClassMetadata for " + entityClass.getName() + " is not SingleTableEntityPersister");
        }
        return ((SingleTableEntityPersister) classMetadata).getTableName();
    }
}
