package ru.runa.wfe.extension.handler.sql;

import lombok.Data;

/**
 * Represents parameter of {@link ru.runa.wfe.extension.handler.sql.SqlHandlerQuery.sqltask.AbstractQuery}Created
 * on 01.04.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@Data
public class SqlHandlerQueryParameter {
    private final String variableName;
    private final String fieldName;
    private final boolean swimlane;

}
