package ru.runa.wfe.extension.handler.sql;

import lombok.Data;

/**
 * 
 * Created on 08.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@Data
public class SqlHandlerQueryResult {
    private final String variableName;
    private final String fieldName;
    private final int outParameterIndex;
    private final boolean swimlane;

}
