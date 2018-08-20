package ru.runa.wfe.commons;

import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * Enumeration contains databases for preliminary {@link DbPatch} support.
 * 
 * @author Dofs
 */
public enum DbType {
    GENERIC, HSQL, ORACLE, POSTGRESQL, MSSQL, MYSQL, H2
}
