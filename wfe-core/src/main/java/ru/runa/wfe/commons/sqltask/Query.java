package ru.runa.wfe.commons.sqltask;

/**
 * Represents Queury in {@link ru.runa.wfe.commons.sqltask.DatabaseTask} Created on 19.05.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class Query extends AbstractQuery {

    public Query(String sql, Parameter[] queries, Result[] results) {
        super(sql, queries, results);
    }
}
