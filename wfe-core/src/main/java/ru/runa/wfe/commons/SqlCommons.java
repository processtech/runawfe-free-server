/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains helper methods for java.sql package.
 *
 */
public final class SqlCommons {
    public static final String ANY_SYMBOLS = "*";
    public static final String ANY_SYMBOL = "?";
    private static final String QUOTED_ANY_SYMBOLS = Pattern.quote(ANY_SYMBOLS);
    private static final String QUOTED_ANY_SYMBOL = Pattern.quote(ANY_SYMBOL);
    private static final String DB_ANY_SYMBOLS = Matcher.quoteReplacement("%");
    private static final String DB_ANY_SYMBOL = Matcher.quoteReplacement("_");
    private static final Log log = LogFactory.getLog(SqlCommons.class);

    public static StringEqualsExpression getStringEqualsExpression(String value) {
        boolean likeExpression = false;
        if (value.contains(ANY_SYMBOLS)) {
            value = value.replaceAll(QUOTED_ANY_SYMBOLS, DB_ANY_SYMBOLS);
            likeExpression = true;
        }
        if (value.contains(ANY_SYMBOL)) {
            value = value.replaceAll(QUOTED_ANY_SYMBOL, DB_ANY_SYMBOL);
            likeExpression = true;
        }
        return new StringEqualsExpression(value, likeExpression);
    }

    /**
     * Closes connection suppressing any thrown exceptions.
     *
     * @param connection
     *            connection to close
     */
    public static void releaseResources(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            log.warn("Failed to release Connection", e);
        }
    }

    /**
     * Closes connection and prepared statement suppressing any thrown exceptions.
     *
     * @param connection
     *            connection to close
     * @param statement
     *            statement to close
     */
    public static void releaseResources(Connection connection, Statement statement) {
        releaseResources(statement);
        releaseResources(connection);
    }

    public static void releaseResources(Connection connection, Statement statement, ResultSet rs) {
        releaseResources(rs);
        releaseResources(statement);
        releaseResources(connection);
    }

    public static void releaseResources(Statement statement, ResultSet rs) {
        releaseResources(rs);
        releaseResources(statement);
    }

    /**
     * Closes prepared statement suppressing any thrown exceptions.
     *
     * @param statement
     *            statement to close
     */
    public static void releaseResources(Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            log.warn("Failed to release Statement", e);
        }
    }

    public static void releaseResources(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.warn("Failed to release ResultSet", e);
        }
    }

    public static class StringEqualsExpression {
        private String value;
        private boolean likeExpression;

        public StringEqualsExpression(String value, boolean likeExpression) {
            this.value = value;
            this.likeExpression = likeExpression;
        }

        public String getValue() {
            return value;
        }

        public String getComparisonOperator() {
            return likeExpression ? "like" : "=";
        }

    }
}
