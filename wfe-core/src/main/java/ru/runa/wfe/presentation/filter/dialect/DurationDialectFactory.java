package ru.runa.wfe.presentation.filter.dialect;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DbType;

public class DurationDialectFactory {

    public static DurationDialect createDialect() {
        final DbType dbtype = ApplicationContextFactory.getDBType();
        if (null != dbtype) {
            switch (dbtype) {
            case H2:
                return new H2DurationDialect();
            case HSQL:
            case MYSQL:
                return new DateDiffDurationDialect();
            case MSSQL:
                return new MsSqlDurationDialect();
            case ORACLE:
                return new Oracle9DurationDialect();
            case POSTGRESQL:
                return new PostgreSqlDurationDialect();
            default:
            }
        }
        return new GenericDurationDialect();
    }
}
