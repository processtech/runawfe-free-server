package ru.runa.wfe.commons.hibernate;

import java.sql.Types;
import org.hibernate.dialect.MySQL5Dialect;

/**
 * @author Alekseev Mikhail
 * @since #1882
 */
public class MySQL5UnicodeDialect extends MySQL5Dialect {
    @Override
    protected void registerVarcharTypes() {
        registerColumnType( Types.VARCHAR, "longtext" );
        registerColumnType( Types.VARCHAR, 16777215, "mediumtext" );
        registerColumnType( Types.VARCHAR, 65535, "varchar(701)" );
        registerColumnType( Types.VARCHAR, 700, "varchar($l)" );
    }
}
