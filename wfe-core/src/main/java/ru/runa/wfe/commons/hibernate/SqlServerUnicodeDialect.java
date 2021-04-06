package ru.runa.wfe.commons.hibernate;

import java.sql.Types;
import org.hibernate.dialect.SQLServer2012Dialect;

/**
 * Maps strings to nvarchar's and unicode support for other types in MSSQL server.
=======
import org.hibernate.Hibernate;
import org.hibernate.dialect.SQLServerDialect;

/**
 * Maps strings to nvarchar's and unicode support for other types in MSSQL
 * server.
>>>>>>> master
 *
 * @author dofs
 * @since 3.5
 */
public class SqlServerUnicodeDialect extends SQLServer2012Dialect {
    /**
     * {@link org.hibernate.dialect.SQLServer2005Dialect#MAX_LENGTH}
     */
    public static final int MAX_LENGTH = 8000;

    public SqlServerUnicodeDialect() {
        registerColumnType(Types.CHAR, "nchar(1)");
        registerColumnType(Types.LONGVARCHAR, "nvarchar(MAX)");
        registerColumnType(Types.CLOB, "nvarchar(MAX)");

        /*
        Types.VARCHAR -> nvarchar(MAX), Types.VARCHAR(MAX) -> nvarchar($l) is important and not a mistake
        because of hibernate type resolving.
        See SQLServer2005Dialect constructor
         */
        registerColumnType(Types.VARCHAR, "nvarchar(MAX)");
        registerColumnType(Types.VARCHAR, MAX_LENGTH, "nvarchar($l)");
    }
}
