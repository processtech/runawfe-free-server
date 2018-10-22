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
package ru.runa.wfe.commons.sqltask;

import com.google.common.base.MoreObjects;

/**
 * Represents Queury in {@link ru.runa.commons.sqltask.DatabaseTask} Created on
 * 01.04.2005 ;-)
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public abstract class AbstractQuery {
    private final String sql;

    private final Parameter[] parameters;

    private final Result[] results;

    /**
     * @param sql
     *            SQL query string
     */
    public AbstractQuery(String sql, Parameter[] queries, Result[] results) {
        this.sql = sql;
        parameters = queries.clone();
        this.results = results.clone();
    }

    public String getSql() {
        return sql;
    }

    public int getParameterCount() {
        return parameters.length;
    }

    public Parameter getParameter(int i) {
        return parameters[i];
    }

    public Result getResultVariable(int i) {
        return results[i];
    }

    public int getResultVariableCount() {
        return results.length;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sql", sql).toString();
    }
}
