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

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.User;

import com.google.common.base.Strings;

public class SQLTreeViewCommand extends JsonAjaxCommand implements InitializingBean {
    private DataSource dataSource;
    private String tableName;
    private Class<?> idsClass = Long.class;
    private String parentIdColumnName;
    private String idColumnName;
    private String labelColumnName;
    private String selectableColumnName;
    private String sqlGetRoots;
    private String sqlGetChildren;
    private String sqlHasChildren;

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setIdsClassName(String idsClassName) {
        this.idsClass = ClassLoaderUtil.loadClass(idsClassName);
    }

    @Required
    public void setParentIdColumnName(String parentIdColumnName) {
        this.parentIdColumnName = parentIdColumnName;
    }

    @Required
    public void setIdColumnName(String idColumnName) {
        this.idColumnName = idColumnName;
    }

    @Required
    public void setLabelColumnName(String labelColumnName) {
        this.labelColumnName = labelColumnName;
    }

    public void setSelectableColumnName(String selectableColumnName) {
        this.selectableColumnName = selectableColumnName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        sqlGetRoots = "SELECT * FROM " + tableName + " WHERE " + parentIdColumnName + " IS NULL";
        sqlGetChildren = "SELECT * FROM " + tableName + " WHERE " + parentIdColumnName + "=?";
        sqlHasChildren = "SELECT COUNT(*) FROM " + tableName + " WHERE " + parentIdColumnName + "=?";
        log.debug("sqlGetRoots = " + sqlGetRoots);
        log.debug("sqlGetChildren = " + sqlGetChildren);
        log.debug("sqlHasChildren = " + sqlHasChildren);
    }

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        PreparedStatement countStatement = null;
        try {
            connection = dataSource.getConnection();
            String parentIdString = request.getParameter("parentId");
            if (Strings.isNullOrEmpty(parentIdString)) {
                statement = connection.prepareStatement(sqlGetRoots);
            } else {
                statement = connection.prepareStatement(sqlGetChildren);
                Object parentId = TypeConversionUtil.convertTo(idsClass, parentIdString);
                statement.setObject(1, parentId);
            }
            countStatement = connection.prepareStatement(sqlHasChildren);
            resultSet = statement.executeQuery();
            JSONArray array = new JSONArray();
            while (resultSet.next()) {
                JSONObject object = new JSONObject();
                Object id = resultSet.getObject(idColumnName);
                object.put("id", id);
                object.put("label", resultSet.getString(labelColumnName));
                object.put("selectable", selectableColumnName != null ? resultSet.getBoolean(selectableColumnName) : true);
                ResultSet countResultSet = null;
                try {
                    countStatement.setObject(1, id);
                    countResultSet = countStatement.executeQuery();
                    countResultSet.next();
                    object.put("hasChildren", countResultSet.getInt(1) > 0);
                } finally {
                    SQLCommons.releaseResources(countResultSet);
                }
                array.add(object);
            }
            if (array.size() > 0) {
                ((JSONObject) array.get(array.size() - 1)).put("last", true);
            }
            return array;
        } finally {
            SQLCommons.releaseResources(countStatement);
            SQLCommons.releaseResources(connection, statement, resultSet);
        }
    }

}
