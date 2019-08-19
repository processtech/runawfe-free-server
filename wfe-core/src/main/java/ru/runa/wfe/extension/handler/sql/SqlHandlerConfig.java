package ru.runa.wfe.extension.handler.sql;

import java.util.List;
import lombok.Data;

/**
 * Created on 01.04.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@Data
public class SqlHandlerConfig {
    private final String dataSourceValue;
    private final List<SqlHandlerQuery> queries;

}
