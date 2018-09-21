package ru.runa.wfe.commons;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Enumeration contains databases for preliminary {@link DbMigration} support.
 * 
 * @author Dofs
 */
public enum DbType {
    HSQL, ORACLE, POSTGRESQL, MSSQL, MYSQL, H2
}
