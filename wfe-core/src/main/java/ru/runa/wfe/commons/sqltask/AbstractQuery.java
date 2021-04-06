package ru.runa.wfe.commons.sqltask;

import com.google.common.base.MoreObjects;

/**
 * Represents Queury in {@link ru.runa.wfe.commons.sqltask.DatabaseTask}.
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public abstract class AbstractQuery {
    private final String sql;

    private final Parameter[] parameters;

    private final Result[] results;

    /**
     * @param sql
     *            SQL query string
     */
    public AbstractQuery(String sql, Parameter[] queries, Result[] results) {
        this.sql = sql;
        parameters = queries.clone();
        this.results = results.clone();
    }

    public String getSql() {
        return sql;
    }

    public int getParameterCount() {
        return parameters.length;
    }

    public Parameter getParameter(int i) {
        return parameters[i];
    }

    public Result getResultVariable(int i) {
        return results[i];
    }

    public int getResultVariableCount() {
        return results.length;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sql", sql).toString();
    }
}
