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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import ru.runa.wfe.chat.UnreadMessagesPresentation;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.presentation.BatchPresentation;

/**
 * Builds query for {@link BatchPresentation}.
 */
public class HibernateCompilerQueryBuilder {

    /**
     * {@link BatchPresentation}, used to build SQL query.
     */
    private final BatchPresentation batchPresentation;

    /**
     * Parameters, used to build query.
     */
    private final CompilerParameters parameters;

    /**
     * Component to build HQL query for {@link BatchPresentation}.
     */
    private final HibernateCompilerHqlBuider hqlBuilder;

    /**
     * {@link Session}, used to create a {@link Query}.
     */
    private final Session session = ApplicationContextFactory.getCurrentSession();

    /**
     * Creates instance of component, used to build {@link Query} for {@link BatchPresentation}.
     *
     * @param batchPresentation
     *            {@link BatchPresentation}, used to build SQL query.
     * @param parameters
     *            Parameters, used to build query.
     */
    public HibernateCompilerQueryBuilder(BatchPresentation batchPresentation, CompilerParameters parameters) {
        hqlBuilder = new HibernateCompilerHqlBuider(batchPresentation, parameters);
        this.parameters = parameters;
        this.batchPresentation = batchPresentation;
    }

    /**
     * Builds {@link Query} for {@link BatchPresentation}.
     *
     * @return {@link Query} to load data from database.
     */
    public Query build() {
        hqlBuilder.build();
        String sqlRequest = translateToSQL();
        if (parameters.isCountQuery() || parameters.isOnlyIdentityLoad()) {
            return session.createSQLQuery(sqlRequest).setResultTransformer(CountIdResultTransformer.INSTANCE);
        } else {
            SQLQuery query = session.createSQLQuery(sqlRequest);
            query.addEntity(batchPresentation.getType().getPresentationClass());
            return query;
        }
    }

    /**
     * Returns Map from SQL positional parameter name to parameter value, generated after build method call.
     *
     * @return Map from SQL positional parameter name to parameter value.
     */
    public QueryParametersMap getPlaceholders() {
        return hqlBuilder.getPlaceholders();
    }

    /**
     * Translates HQL from hqlBuilder to SQL and makes ordering and filtering inheritance tuning.
     *
     * @return SQL query string.
     */
    private String translateToSQL() {
        HibernateCompilerTranslator queryTranslator = new HibernateCompilerTranslator(hqlBuilder.getQuery(), parameters.isCountQuery());
        List<String> phSeq = HibernateCompilerPlaceholdersHelper.getPlaceholdersFromHQL(hqlBuilder.getQuery(), hqlBuilder.getPlaceholders());
        StringBuilder sqlRequest = new StringBuilder(queryTranslator.translate());
        HibernateCompilerPlaceholdersHelper.restorePlaceholdersInSQL(sqlRequest, phSeq);
        sqlRequest = tuneSelectClause(sqlRequest);
        new HibernateCompilerInheritanceFiltersBuilder(batchPresentation, hqlBuilder, queryTranslator).injectFiltersStatements(sqlRequest);
        new HibernateCompilerInheritanceOrderBuilder(batchPresentation, hqlBuilder, queryTranslator).injectOrderStatements(sqlRequest);
        new HibernateCompilerLeftJoinBuilder(batchPresentation).injectLeftJoin(sqlRequest);
        return sqlRequest.toString();
    }

    /**
     * Removes from SQL select clause all column names and 'as' statements. So it's converts SQL like 'select TABLE.ID as T_ID, TABLE.NAME as T_N' to
     * 'select TABLE.*'. Actually only 'as' statements removing is required, but removing all columns is much simple and resulting request is much
     * clever.
     *
     * But for joined table generate new aliases.
     *
     * @param sqlRequest
     *            SQL request to tune select clause.
     */
    private StringBuilder tuneSelectClause(StringBuilder sqlRequest) {
        if (parameters.isCountQuery() || parameters.isOnlyIdentityLoad()) {
            return sqlRequest;
        }
        int posDot = sqlRequest.indexOf(".");
        int posFrom = HibernateCompilerHelper.getFromClauseIndex(sqlRequest);
        sqlRequest.replace(posDot + 1, posFrom, "*");
        return (parameters.getAdditionalSelectClauses().isEmpty()) ? sqlRequest : addAdditionalSelectClauses(sqlRequest);
    }

    private StringBuilder addAdditionalSelectClauses(StringBuilder sqlRequest) {
        String unreadMessagesExecutorId = "";
        for (String clause : parameters.getAdditionalSelectClauses()) {
            if (clause.contains(UnreadMessagesPresentation.UNREAD_MESSAGES_EXECUTOR_ID)) {
                unreadMessagesExecutorId = clause;
            }
            String clauseFromSql = (sqlRequest.indexOf(clause) == -1)
                    ? getClauseFromSql(sqlRequest, clause)
                    : clause;
            if (!clauseFromSql.equals("")) {
                int idx = sqlRequest.indexOf("*");
                sqlRequest.insert(idx + 1, ", " + clauseFromSql);
            }
        }
        if (!unreadMessagesExecutorId.equals("") && sqlRequest.indexOf(UnreadMessagesPresentation.UNREAD_MESSAGES_EXECUTOR_ID) != -1) {
            String userId = unreadMessagesExecutorId.substring(unreadMessagesExecutorId.indexOf("=") + 1);
            return new StringBuilder(sqlRequest.toString().replace(UnreadMessagesPresentation.UNREAD_MESSAGES_EXECUTOR_ID, userId));
        }
        return sqlRequest;
    }

    /**
     * @param sql
     *          sql, in which clause is searched.
     * @param clause
     *          clause, that is searched for in sql.
     * @return clause if found and empty string otherwise.
     */
    private String getClauseFromSql(StringBuilder sql, String clause) {
        if (clause.matches(".*[0-9].*")) {
            Matcher m = Pattern.compile("[0-9]").matcher(clause);
            if (m.find()) {
                int numberPosition = m.start();
                String beforeNumber = clause.substring(0, numberPosition);
                String afterNumber = clause.substring(numberPosition + 1);
                int beforeNumberIndex = sql.indexOf(beforeNumber);
                int afterNumberIndex = sql.indexOf(afterNumber);
                if (beforeNumberIndex == -1 || afterNumberIndex == -1) {
                    return "";
                } else {
                    while (afterNumberIndex - beforeNumberIndex > beforeNumber.length() * 2) {
                        beforeNumberIndex = sql.indexOf(beforeNumber, beforeNumberIndex + beforeNumber.length());
                    }
                    return sql.substring(beforeNumberIndex, afterNumberIndex) + afterNumber;
                }
            }
        }
        return "";
    }

    /**
     * Used to load object's count and object's identities query. Oracle in object's identities query if setFirstResult is not 0 returns tuple:
     * [object id; row id].
     */
    static class CountIdResultTransformer implements ResultTransformer {
        private static final long serialVersionUID = 1L;

        public static final CountIdResultTransformer INSTANCE = new CountIdResultTransformer();

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            return tuple[0];
        }

        @Override
        public List transformList(List collection) {
            return collection;
        }
    }
}
