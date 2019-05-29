package ru.runa.wfe.commons;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Enumeration contains databases for preliminary {@link DbMigration} support.
 * 
 * @author Dofs
 */
public enum DbType {
    // TODO Delete MYSQL in WFE 5. BTW it does not work in WFE 4 anyway, see comment inside JbpmRefactoringPatch.
    HSQL, ORACLE, POSTGRESQL, MSSQL, MYSQL, H2
}
