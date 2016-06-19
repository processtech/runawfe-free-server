package ru.runa.wfe.presentation.filter.dialect;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;

public class DurationDialectFactory {

    public static IDurationDialect createDialect() {
        final DBType dbtype = ApplicationContextFactory.getDBType();
        if (null != dbtype) {
            switch (dbtype) {
            case H2:
            case HSQL:
            case MYSQL:
                return new DateDiffDurationDialect();
            case MSSQL:
                return new MSSqlDurationDialect();
            case ORACLE:
                return new Oracle9DurationDialect();
            case POSTGRESQL:
                return new PostgreSQLDurationDialect();
            default:
            }
        }
        return new GenericDurationDialect();
    }
}
