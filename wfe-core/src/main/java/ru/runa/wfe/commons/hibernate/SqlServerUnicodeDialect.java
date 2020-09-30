package ru.runa.wfe.commons.hibernate;

import java.sql.Types;
import org.hibernate.dialect.SQLServer2012Dialect;

/**
 * Maps strings to nvarchar's and unicode support for other types in MSSQL server.
 *
 * @author dofs
 * @since 3.5
 */
public class SqlServerUnicodeDialect extends SQLServer2012Dialect {

    public SqlServerUnicodeDialect() {
        registerColumnType(Types.CHAR, "nchar(1)");
        registerColumnType(Types.VARCHAR, "nvarchar($l)");
        registerColumnType(Types.LONGVARCHAR, "nvarchar($l)");
        registerColumnType(Types.CLOB, "ntext");
    }
}
