package ru.runa.wfe.commons;

import lombok.RequiredArgsConstructor;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Enumeration contains databases for preliminary {@link DbMigration} support.
 * 
 * @author Dofs
 */
@RequiredArgsConstructor
public enum DbType {
    H2(false),
    HSQL(false),
    MSSQL(false),
    MYSQL(false),  // TODO Delete in WFE 5. BTW it does not work in WFE 4 anyway, see comment inside JbpmRefactoringPatch.
    ORACLE(false),
    POSTGRESQL(true);

    public final boolean hasTransactionalDDL;
}
