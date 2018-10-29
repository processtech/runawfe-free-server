package ru.runa.wfe.presentation.hibernate;

import java.util.List;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
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
        if (parameters.isCountQuery()) {
            return session.createSQLQuery(sqlRequest).setResultTransformer(SingleValueResultTransformer.INSTANCE);
        }
        String[] hqlFields = parameters.getOnlySpecificHqlFields();
        if (hqlFields != null) {
            return session.createSQLQuery(sqlRequest).setResultTransformer(
                    hqlFields.length == 1 ? SingleValueResultTransformer.INSTANCE : TupleResultTransformer.INSTANCE
            );
        }

        SQLQuery query = session.createSQLQuery(sqlRequest);
        query.addEntity(batchPresentation.getType().getPresentationClass());
        return query;
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
        if (parameters.isCountQuery() || parameters.getOnlySpecificHqlFields() != null) {
            return sqlRequest;
        }
        int posDot = sqlRequest.indexOf(".");
        int posFrom = HibernateCompilerHelper.getFromClauseIndex(sqlRequest);
        return sqlRequest.replace(posDot + 1, posFrom, "*");
    }

    /**
     * Used to load object's count and object's identities query. Oracle in object's identities query if setFirstResult is not 0 returns tuple:
     * [object id; row id].
     */
    static class SingleValueResultTransformer implements ResultTransformer {
        private static final long serialVersionUID = 1L;

        public static final SingleValueResultTransformer INSTANCE = new SingleValueResultTransformer();

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            return tuple[0];
        }

        @Override
        public List transformList(List collection) {
            return collection;
        }
    }

    /**
     * Used to load object's count and object's identities query. Oracle in object's identities query if setFirstResult is not 0 returns tuple:
     * [object id; row id].
     */
    static class TupleResultTransformer implements ResultTransformer {
        private static final long serialVersionUID = 1L;

        public static final TupleResultTransformer INSTANCE = new TupleResultTransformer();

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            return tuple;
        }

        @Override
        public List transformList(List collection) {
            return collection;
        }
    }
}
