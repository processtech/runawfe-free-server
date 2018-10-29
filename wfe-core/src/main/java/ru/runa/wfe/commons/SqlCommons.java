package ru.runa.wfe.commons;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.apachecommons.CommonsLog;

/**
 * Contains helper methods for java.sql package.
 *
 */
@CommonsLog
public final class SqlCommons {
    public static final String ANY_SYMBOLS = "*";
    public static final String ANY_SYMBOL = "?";
    private static final String QUOTED_ANY_SYMBOLS = Pattern.quote(ANY_SYMBOLS);
    private static final String QUOTED_ANY_SYMBOL = Pattern.quote(ANY_SYMBOL);
    private static final String DB_ANY_SYMBOLS = Matcher.quoteReplacement("%");
    private static final String DB_ANY_SYMBOL = Matcher.quoteReplacement("_");

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
