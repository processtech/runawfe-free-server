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

/**
 * Created on 01.04.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class DatabaseTask {
    public final static String INSTANCE_ID_VARIABLE_NAME = "instanceId";
    public final static String CURRENT_DATE_VARIABLE_NAME = "currentDate";
    private final String datasourceName;
    private final AbstractQuery[] queries;

    public DatabaseTask(String datasourceName, AbstractQuery[] queries) {
        this.datasourceName = datasourceName;
        this.queries = queries.clone();
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public int getQueriesCount() {
        return queries.length;
    }

    public AbstractQuery getQuery(int i) {
        return queries[i];
    }
}
