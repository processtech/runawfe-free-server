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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Allows to execute arbitrary sql from task form.
 * 
 * @author Dofs
 * @since 4.3.0
 */
public class SqlAjaxCommand extends JsonAjaxCommand {
    private DataSource dataSource;
    private String sql;
    private List<Parameter> parameters = Lists.newArrayList();
    private List<Result> results = Lists.newArrayList();

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setSql(String sql) {
        this.sql = sql.trim();
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            int parameterIndex = 1;
            StringBuilder parametersInfo = new StringBuilder();
            for (Parameter parameter : parameters) {
                String string = request.getParameter(parameter.name);
                Object value = TypeConversionUtil.convertTo(parameter.clazz, string);
                parametersInfo.append(" ").append(parameter.name).append(":").append(string).append(":").append(parameter.clazz.getName());
                statement.setObject(parameterIndex++, value);
            }
            log.debug("Executing sql " + sql + " with parameters " + parametersInfo);
            resultSet = statement.executeQuery();
            JSONArray array = new JSONArray();
            while (resultSet.next()) {
                JSONObject object = new JSONObject();
                for (Result result : results) {
                    Object fieldData = resultSet.getObject(result.columnName);
                    if (fieldData instanceof Date) {
                        fieldData = ((Date) fieldData).getTime();
                    }
                    object.put(result.propertyName, fieldData);
                }
                array.add(object);
            }
            log.debug("Returning " + array);
            return array;
        } finally {
            SqlCommons.releaseResources(connection, statement, resultSet);
        }
    }

    public static class Parameter {
        private String name;
        private Class<?> clazz;

        @Required
        public void setName(String name) {
            this.name = name;
        }

        @Required
        public void setJavaType(String javaType) {
            this.clazz = ClassLoaderUtil.loadClass(javaType);
        }

    }

    public static class Result {
        private String propertyName;
        private String columnName;

        @Required
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        @Required
        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

    }
}
