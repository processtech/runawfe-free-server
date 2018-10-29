package ru.runa.wfe.commons.sqltask;

/**
 * Represents Queury in {@link ru.runa.wfe.commons.sqltask.DatabaseTask}
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class StoredProcedureQuery extends AbstractQuery {

    public StoredProcedureQuery(String sql, Parameter[] queries, Result[] results) {
        super(sql, queries, results);
    }
}
