package ru.runa.wfe.commons.sqltask;

/**
 * Created on 01.04.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class DatabaseTask {
    public final static String INSTANCE_ID_VARIABLE_NAME = "instanceId";
    public final static String CURRENT_DATE_VARIABLE_NAME = "currentDate";
    private final String datasourceName;
    private final AbstractQuery[] queries;

    public DatabaseTask(String datasourceName, AbstractQuery[] queries) {
        this.datasourceName = datasourceName;
        this.queries = queries.clone();
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public int getQueriesCount() {
        return queries.length;
    }

    public AbstractQuery getQuery(int i) {
        return queries[i];
    }
}
