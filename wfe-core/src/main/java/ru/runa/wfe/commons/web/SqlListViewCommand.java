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
 */package ru.runa.wfe.commons.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.user.User;

public class SqlListViewCommand extends JsonAjaxCommand implements InitializingBean {
    private DataSource dataSource;
    private String tableName;
    private String valueColumnName;
    private String labelColumnName;
    private int maxSize = 10;
    private String sql;

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Required
    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    @Required
    public void setLabelColumnName(String labelColumnName) {
        this.labelColumnName = labelColumnName;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        sql = "SELECT * FROM " + tableName + " WHERE " + labelColumnName + " LIKE ? ORDER BY " + labelColumnName + ", " + valueColumnName;
        log.debug("sql = " + sql);
    }

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            String filter = request.getParameter("filter");
            if (filter == null) {
                filter = "";
            }
            filter += "%";
            statement = connection.prepareStatement(sql);
            statement.setObject(1, filter);
            resultSet = statement.executeQuery();
            JSONArray array = new JSONArray();
            int counter = 0;
            while (resultSet.next()) {
                JSONObject object = new JSONObject();
                object.put("value", resultSet.getObject(valueColumnName));
                object.put("label", resultSet.getString(labelColumnName));
                array.add(object);
                counter++;
                if (counter == maxSize) {
                    break;
                }
            }
            return array;
        } finally {
            SqlCommons.releaseResources(connection, statement, resultSet);
        }
    }

}
