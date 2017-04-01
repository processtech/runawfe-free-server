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

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;

/**
 * Class for applies left join to SQL query.
 */
public class HibernateCompilerLeftJoinBuilder {

    /**
     * {@link BatchPresentation}, used to build SQL query.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Creates instance to applies left join to SQL query.
     *
     * @param batchPresentation
     *            {@link BatchPresentation}, used to build SQL query.
     */
    public HibernateCompilerLeftJoinBuilder(BatchPresentation batchPresentation) {
        this.batchPresentation = batchPresentation;
    }

    /**
     * Injects left joins into SQL query.
     *
     * @param sqlRequest
     *            SQL query to inject left joins.
     */
    public void injectLeftJoin(StringBuilder sqlRequest) {
        List<LeftJoinDescription> leftJoins = new ArrayList<LeftJoinDescription>();
        for (FieldDescriptor field : batchPresentation.getAllFields()) {
            if (field.displayName.startsWith(ClassPresentation.editable_prefix)) {
                continue;
            }
            if (field.dbSources[0].getSourceObject().equals(batchPresentation.getClassPresentation().getPresentationClass())) {
                continue;
            }
            if (!HibernateCompilerHelper.isFieldSQLAffects(field, batchPresentation)) {
                continue;
            }
            LeftJoinDescription left = applyLeftJoin(sqlRequest, field);
            if (left != null) {
                leftJoins.add(left);
            }
        }
        int fromIdx = HibernateCompilerHelper.getFromClauseIndex(sqlRequest);
        for (LeftJoinDescription left : leftJoins) {
            int insertIdx = sqlRequest.indexOf(left.rootTableName, fromIdx) + left.rootTableName.length();
            sqlRequest.insert(insertIdx, " ").insert(insertIdx + 1, left.leftJoinExpression);
        }
    }

    /**
     * Removes join for field and replaces it with left join.
     *
     * @param sqlRequest
     *            SQL query to inject left joins.
     * @param field
     *            Field for replace join to left join.
     * @return Left join, applied to query.
     */
    private LeftJoinDescription applyLeftJoin(StringBuilder sqlRequest, FieldDescriptor field) {
        final String tableName = HibernateCompilerHelper.getTableName(field.dbSources[0].getSourceObject());
        if (sqlRequest.indexOf(tableName) < 0) {
            return null;
        }
        final String joinedTableAlias = HibernateCompilerHelper.getIdentifier(sqlRequest, tableName, true);
        removeJoinedTable(sqlRequest, tableName);
        String restriction = getJoinRestriction(sqlRequest, joinedTableAlias);
        String rootJoinTable = getRootJoinTable(joinedTableAlias, restriction);
        String leftJoinQuery = null;
        if (field.displayName.startsWith(ClassPresentation.removable_prefix)) {
            String condition = getRemovableFieldCondition(sqlRequest, joinedTableAlias);
            leftJoinQuery = " left join (select " + joinedTableAlias + ".* from " + tableName + " " + joinedTableAlias + " where " + condition + ")"
                    + joinedTableAlias + " on " + restriction + " ";
        } else {
            leftJoinQuery = " left join " + tableName + " " + joinedTableAlias + " on " + restriction + " ";
        }
        return new LeftJoinDescription(leftJoinQuery, rootJoinTable);
    }

    /**
     * Get from query condition statement for removable field. E. q. it returns something like this: (var.name = 'name').
     *
     * @param sqlRequest
     *            SQL query to inject left joins.
     * @param joinedTableAlias
     *            SQL alias name, assigned to joining table.
     * @return Condition statement for removable field.
     */
    private String getRemovableFieldCondition(StringBuilder sqlRequest, String joinedTableAlias) {
        int conditionTo = sqlRequest.indexOf(joinedTableAlias);
        int conditionFrom = sqlRequest.lastIndexOf("(", conditionTo);
        boolean inQuot = false;
        while (sqlRequest.charAt(conditionTo) != ')' || inQuot) {
            if (sqlRequest.charAt(conditionTo) == '\'') {
                inQuot = !inQuot;
            }
            ++conditionTo;
        }
        ++conditionTo;
        String condition = sqlRequest.substring(conditionFrom, conditionTo);
        sqlRequest.replace(conditionFrom, conditionTo, "(1=1)");
        return condition;
    }

    /**
     * Get root join table alias from join restriction statement. Root join table is a table, we joining to (it must be placed right before 'left
     * join' statement).
     *
     * @param joinedTableAlias
     *            SQL alias name, assigned to joining table.
     * @param restriction
     *            Join restriction statement.
     * @return Root join table alias.
     */
    private String getRootJoinTable(String joinedTableAlias, String restriction) {
        int idx = restriction.indexOf('.');
        String identifier = HibernateCompilerHelper.getIdentifier(restriction, idx - 1, false);
        if (!identifier.equals(joinedTableAlias)) {
            return identifier;
        }
        idx = restriction.indexOf('.', idx + 1); // Next '.' in restriction. If first with joined table, when second is for root join table.
        return HibernateCompilerHelper.getIdentifier(restriction, idx - 1, false);
    }

    /**
     * Get joins restrictions. E. q. something like processInst.ID = variableInst.ID
     *
     * @param sqlRequest
     *            SQL query to inject left joins.
     * @param joinedTableAlias
     *            SQL alias name, assigned to joining table.
     * @return Join restriction statement.
     */
    private String getJoinRestriction(StringBuilder sqlRequest, String joinedTableAlias) {
        String restriction;
        // Join Restriction conditions must be in result query first
        int fromIndex = HibernateCompilerHelper.getFromClauseIndex(sqlRequest);
        int restrFrom = -1;
        int restrictionIndex = fromIndex;
        while (-1 == restrFrom) {
            restrictionIndex = sqlRequest.indexOf(joinedTableAlias, restrictionIndex);
            if (-1 == restrictionIndex) {
                restrFrom = fromIndex;
                break;
            }
            restrFrom = sqlRequest.lastIndexOf("(", restrictionIndex);
            restrictionIndex += joinedTableAlias.length();
        }
        restrFrom = sqlRequest.lastIndexOf("(", restrFrom - 1);
        int restrTo = restrFrom + 1;
        boolean inQuot = false;
        int openBraced = 1;
        while (openBraced > 0) {
            if (sqlRequest.charAt(restrTo) == '\'') {
                inQuot = !inQuot;
            }
            if (sqlRequest.charAt(restrTo) == '(' && !inQuot) {
                ++openBraced;
            }
            if (sqlRequest.charAt(restrTo) == ')' && !inQuot) {
                --openBraced;
            }
            ++restrTo;
        }
        restriction = sqlRequest.substring(restrFrom, restrTo);
        if (restriction.indexOf(" and ") != -1) {
            restriction = restriction.substring(0, restriction.indexOf(" and ")) + "))";
        }
        if (restriction.indexOf(" AND ") != -1) {
            restriction = restriction.substring(0, restriction.indexOf(" AND ")) + "))";
        }
        sqlRequest.replace(restrFrom, restrTo, "(1=1)");
        return restriction;
    }

    /**
     * Remove left joining table from 'from' clause. It will be added there later with left join.
     *
     * @param sqlRequest
     *            SQL request to remove joined table.
     * @param tableName
     *            Table name, which must be left joined.
     */
    private void removeJoinedTable(StringBuilder sqlRequest, String tableName) {
        int fromIndex = HibernateCompilerHelper.getFromClauseIndex(sqlRequest);
        int fromPosition = sqlRequest.indexOf(tableName, fromIndex);
        int toPosition = sqlRequest.indexOf(",", fromPosition);
        int tmpPos = sqlRequest.indexOf(" where ", fromPosition);
        if ((tmpPos != -1 && tmpPos < toPosition) || toPosition == -1) {
            toPosition = tmpPos;
        }
        tmpPos = sqlRequest.indexOf(" order by ", fromPosition);
        if ((tmpPos != -1 && tmpPos < toPosition) || toPosition == -1) {
            toPosition = tmpPos;
        }
        tmpPos = sqlRequest.indexOf(" left ", fromPosition);
        if ((tmpPos != -1 && tmpPos < toPosition) || toPosition == -1) {
            toPosition = tmpPos;
        }
        if (toPosition == -1) {
            toPosition = sqlRequest.length();
        }
        if (sqlRequest.charAt(toPosition) == ',') {
            ++toPosition;
        } else {
            int tmp = fromPosition - 1;
            while (tmp >= 0 && sqlRequest.charAt(tmp) == ' ') {
                --tmp;
            }
            if (tmp >= 0 && sqlRequest.charAt(tmp) == ',') {
                fromPosition = tmp;
            }
        }
        sqlRequest.replace(fromPosition, toPosition, " ");
    }
}
