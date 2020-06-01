package ru.runa.wfe.extension.handler.sql;

import java.util.List;
import lombok.Data;

/**
 * Represents Query in {@link ru.runa.wfe.extension.handler.sql.SqlHandlerConfig}
 * 
 * Created on 01.04.2005 ;-)
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@Data
public class SqlHandlerQuery {
    private final String sql;
    private final List<SqlHandlerQueryParameter> parameters;
    private final List<SqlHandlerQueryResult> results;
    private final boolean storedProcedureQuery;

}
