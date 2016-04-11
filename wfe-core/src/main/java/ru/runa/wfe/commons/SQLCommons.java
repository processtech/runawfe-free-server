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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Contains helper methods for java.sql package.
 * 
 */
public final class SQLCommons {
    private static final Log log = LogFactory.getLog(SQLCommons.class);

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
     * Closes connection and prepared statement suppressing any thrown
     * exceptions.
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

}
