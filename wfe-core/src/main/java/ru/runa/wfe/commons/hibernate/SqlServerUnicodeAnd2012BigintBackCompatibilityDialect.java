package ru.runa.wfe.commons.hibernate;

import java.sql.Types;

/**
 * Compound of {@link SqlServerUnicodeDialect} and {@link SqlServer2012BigintBackCompatibilityDialect}
 *
 * @author Alekseev Mikhail
 * @since #1882
 */
public class SqlServerUnicodeAnd2012BigintBackCompatibilityDialect extends SqlServerUnicodeDialect {
    public SqlServerUnicodeAnd2012BigintBackCompatibilityDialect() {
        registerColumnType(Types.BIGINT, "numeric(19, 0)");
    }
}
