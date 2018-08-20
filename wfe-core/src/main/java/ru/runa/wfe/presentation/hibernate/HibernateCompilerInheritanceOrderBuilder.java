/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.presentation.hibernate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.DbSource;
import ru.runa.wfe.presentation.DbSource.AccessType;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldState;

/**
 * Builds order by SQL statements for fields with inheritance.
 */
class HibernateCompilerInheritanceOrderBuilder {

    /**
     * {@link BatchPresentation}, used to build query.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Component to build HQL query for {@link BatchPresentation}.
     */
    private final HibernateCompilerHqlBuider hqlBuilder;

    /**
     * Translator, used to translate HQL query to SQL.
     */
    private final HibernateCompilerTranslator queryTranslator;

    /**
     * Constructs instance for building order by SQL statements for fields with inheritance.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to build query.
     * @param hqlBuilder
     *            Component to build HQL query for {@link BatchPresentation}.
     * @param queryTranslator
     *            Translator, used to translate HQL query to SQL.
     */
    HibernateCompilerInheritanceOrderBuilder(BatchPresentation batchPresentation, HibernateCompilerHqlBuider hqlBuilder,
            HibernateCompilerTranslator queryTranslator) {
        this.batchPresentation = batchPresentation;
        this.hqlBuilder = hqlBuilder;
        this.queryTranslator = queryTranslator;
    }

    /**
     * Injects SQL statements to order by fields with inheritance into SQL query.
     * 
     * @param sqlQuery
     *            SQL query to inject order statements.
     */
    void injectOrderStatements(StringBuilder sqlQuery) {
        if (!hqlBuilder.isOrderByInheritance()) {
            return;
        }
        String[] orders = buildSQLOrderClause().toArray(new String[] {});
        int[] inheritedFieldPositions = getInheritanceFieldIndexes(orders);
        {
            boolean hasOrderBySimpleField = getFieldPositionInSql(sqlQuery, inheritedFieldPositions);
            for (int i = inheritedFieldPositions.length - 1; i >= 0; --i) {
                if (inheritedFieldPositions[i] == sqlQuery.length() || sqlQuery.charAt(inheritedFieldPositions[i]) == ',') {
                    sqlQuery.insert(inheritedFieldPositions[i], (hasOrderBySimpleField || i > 0 ? ", " : "") + orders[i]);
                } else {
                    sqlQuery.insert(inheritedFieldPositions[i], orders[i] + (hasOrderBySimpleField || i > 0 ? ", " : ""));
                }
            }
        }
    }

    /**
     * Fills inheritedFieldPositions with actual positions (absolute position in SQL query) of sorted field with inheritance.
     * 
     * @param sqlQuery
     *            SQL query to inject order statements.
     * @param inheritedFieldPositions
     *            Position (order position) of sorted field with inheritance.
     * @return true, if SQL query has order by clause with non inherited fields and false otherwise.
     */
    private boolean getFieldPositionInSql(StringBuilder sqlQuery, int[] inheritedFieldPositions) {
        boolean hasOrderBy = true;
        int currentOffset = sqlQuery.indexOf(" order by ");
        if (currentOffset < 0) {
            hasOrderBy = false;
            sqlQuery.append(" order by ");
            currentOffset = sqlQuery.length();
        } else {
            currentOffset += 10;
        }
        for (int i = 0; i < inheritedFieldPositions.length; ++i) {
            if (i != inheritedFieldPositions[i]) { // Search for right place to insert ordered fields
                while (i != inheritedFieldPositions[i]) {
                    currentOffset = sqlQuery.indexOf(",", currentOffset + 1);
                    if (currentOffset < 0) {
                        currentOffset = sqlQuery.length();
                    }
                    --inheritedFieldPositions[i];
                }
            }
            inheritedFieldPositions[i] = currentOffset;
        }
        return hasOrderBy;
    }

    /**
     * Calculates position of sorted field with inheritance in result order by SQL statement. For example if we sort {id, variable, variable}, result
     * will be {1, 2}. If we sort {variable, id, name, variable}, result will be {0, 3}. This calculation need to insert statement into correct
     * position in result SQL query.
     * 
     * @param orders
     *            Statements, which will be inserted into order by clause.
     * @return Position (order position) of sorted field with inheritance.
     */
    private int[] getInheritanceFieldIndexes(String[] orders) {
        FieldDescriptor[] sortedFields = batchPresentation.getSortedFields();
        int[] sfIdx = new int[orders.length];
        int idx = 0;
        for (int i = 0; i < sortedFields.length; ++i) {
            if (sortedFields[i].dbSources.length > 1) {
                sfIdx[idx++] = i;
            }
        }
        return sfIdx;
    }

    /**
     * Builds order statements for fields with inheritance.
     * 
     * @return List of statements for fields with inheritance.
     */
    private List<String> buildSQLOrderClause() {
        List<String> orderClause = new LinkedList<>();
        FieldDescriptor[] sortedFields = batchPresentation.getSortedFields();
        boolean[] fieldsToSortModes = batchPresentation.getFieldsToSortModes();
        if (sortedFields.length <= 0) {
            return orderClause;
        }
        for (int i = 0; i < sortedFields.length; i++) {
            if (!sortedFields[i].sortable || sortedFields[i].fieldState == FieldState.DISABLED) {
                continue;
            }
            if (sortedFields[i].dbSources.length == 1) {
                continue; // Must be processed before (see HibernateCompilerHQLBuilder)
            }
            List<String> orders = buildOrderToField(sortedFields[i], fieldsToSortModes[i]);
            if (orders.isEmpty()) {
                continue;
            }
            Iterator<String> iterator = orders.iterator();
            StringBuilder builder = new StringBuilder(iterator.next());
            while (iterator.hasNext()) {
                builder.append(", ").append(iterator.next());
            }
            orderClause.add(builder.toString());
        }
        return orderClause;
    }

    /**
     * Creates list of statements to add to order by clause.
     * 
     * @param field
     *            Field to create order by statements.
     * @param sortingMode
     *            true, if sorting is ascending and false for descending.
     * @return List of statements to add to order by clause.
     */
    private List<String> buildOrderToField(FieldDescriptor field, boolean sortingMode) {
        List<String> result = new LinkedList<>();
        for (DbSource dbSource : field.dbSources) {
            String alias = hqlBuilder.getAliasMapping().getAlias(field);
            if (dbSource.getValueDBPath(AccessType.ORDER, null) == null) {
                continue;
            }
            String fakeHQLRequest = "select " + alias + " from " + dbSource.getSourceObject().getName() + " as " + alias + " order by "
                    + dbSource.getValueDBPath(AccessType.ORDER, alias) + (sortingMode ? " asc" : " desc");
            HibernateCompilerTranslator fakeTranslator = new HibernateCompilerTranslator(fakeHQLRequest, false);
            String compiledFakeSQL = fakeTranslator.translate();
            int sortExprIdx = compiledFakeSQL.indexOf("order by") + 9;
            result.add(compiledFakeSQL.substring(sortExprIdx).replaceAll(fakeTranslator.getAliasName(alias), queryTranslator.getAliasName(alias)));
        }
        return result;
    }
}
