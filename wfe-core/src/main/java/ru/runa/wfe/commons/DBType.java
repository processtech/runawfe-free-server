package ru.runa.wfe.commons;

import ru.runa.wfe.commons.dbpatch.DBPatch;

/**
 * Enumeration contains databases for preliminary {@link DBPatch} support.
 * 
 * @author Dofs
 */
public enum DBType {
    GENERIC, HSQL, ORACLE, POSTGRESQL, MSSQL, MYSQL, H2
}
