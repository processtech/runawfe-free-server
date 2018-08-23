package ru.runa.wfe.commons.hibernate;

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;

/**
 * Maps strings to nvarchar's and unicode support for other types in MSSQL
 * server.
 * 
 * @author dofs
 * @since 3.5
 */
public class SqlServerUnicodeDialect extends SQLServerDialect {

    public SqlServerUnicodeDialect() {
        registerColumnType(Types.CHAR, "nchar(1)");
        registerColumnType(Types.VARCHAR, "nvarchar($l)");
        registerColumnType(Types.LONGVARCHAR, "nvarchar($l)");
        registerColumnType(Types.CLOB, "ntext");
    }

}
