package ru.runa.wfe.commons.hibernate;

import java.sql.Types;
import org.hibernate.dialect.SQLServer2012Dialect;

/**
 * Maps {@link Types#BIGINT} to numeric MSSQL type.
 * <p>
 * Should use for migration from RunaWFE Free to RunaWFE Professional/Industrial
 * as RunaWFE Free uses numeric for PK/FK columns {@link org.hibernate.dialect.SQLServerDialect}
 * which leads to errors in FK creation.
 *
 * @author Alekseev Mikhail
 * @since #1882
 */
public class SqlServer2012BigintBackCompatibilityDialect extends SQLServer2012Dialect {
    public SqlServer2012BigintBackCompatibilityDialect() {
        registerColumnType(Types.BIGINT, "numeric(19, 0)");
    }
}
