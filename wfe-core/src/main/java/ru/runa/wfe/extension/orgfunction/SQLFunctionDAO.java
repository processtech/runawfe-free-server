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
package ru.runa.wfe.extension.orgfunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SQLCommons;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Created on 10.05.2005
 * 
 */
public class SQLFunctionDAO {
    private static List<Long> directorsCodesList;

    /**
     * Returns codes of actors selected by sql query. e.g. chief query - select
     * BOSS_ID from EMPLOYEES where ID = ? e.g. direct subordinate query -
     * select ID from EMPLOYEES where "BOSS_ID" = ?
     * 
     * @param sql
     *            sql query
     * @param parameters
     *            parameters of query
     * @return codes of actors
     */
    public static List<Long> getActorCodes(String sql, Object[] parameters) {
        Preconditions.checkNotNull(parameters);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            setParameters(ps, parameters);
            ResultSet rs = ps.executeQuery();
            return getCodesFromResultSet(rs);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            SQLCommons.releaseResources(con, ps);
        }
    }

    /**
     * Returns codes of actors selected by sql query recursevly. e.g. indirect
     * subordinate org function query - select ID from EMPLOYEES where "BOSS_ID"
     * = ? will return all subordinates of employee with given ID and
     * subordinates on those subordinates and so on.
     * 
     * @param sql
     *            sql query
     * @param parameters
     *            parameters of query
     * @return codes of actors
     */
    public static List<Long> getActorCodesRecurisve(String sql, Object[] parameters) {
        Preconditions.checkNotNull(parameters);
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            List<Long> codeSet = Lists.newArrayList();
            getCodesRecursive(ps, codeSet, parameters);
            return codeSet;
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            SQLCommons.releaseResources(con, ps);
        }
    }

    private static void getCodesRecursive(PreparedStatement ps, List<Long> codeSet, Object[] parameters) throws SQLException {
        setParameters(ps, parameters);
        ResultSet rs = ps.executeQuery();
        List<Long> codeList = getCodesFromResultSet(rs);
        rs.close();
        for (int i = 0; i < codeList.size(); i++) {
            Long code = codeList.get(i);
            if (!codeSet.contains(code)) {
                if (!codeSet.add(code)) {
                    // i.e. we have circle in hierarchy
                    throw new InternalApplicationException("Code hierarchy contains cycle");
                }
                parameters[0] = code;
                getCodesRecursive(ps, codeSet, parameters);
            }
        }
    }

    private static void initializeDirectorCodesList(String sql) {
        Connection con = null;
        PreparedStatement ps = null;
        try {
            con = getConnection();
            ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            directorsCodesList = getCodesFromResultSet(rs);
        } catch (SQLException e) {
            throw Throwables.propagate(e);
        } finally {
            SQLCommons.releaseResources(con, ps);
        }
    }

    public static List<Long> getDirectorCode(String sql, String chiefSQL, Long code) {
        List<Long> result = Lists.newArrayList();
        if (directorsCodesList == null) {
            initializeDirectorCodesList(sql);
        }
        if (directorsCodesList.contains(code)) {
            result.add(code);
            return result;
        }

        ArrayList<Long> codes = new ArrayList<Long>();
        codes.add(code);
        if (directorsCodesList.contains((codes.get(0)).longValue())) {
            result.add(codes.get(0));
            return result;
        }
        while (codes.size() > 0) {
            List<Long> chiefsCodes = getActorCodes(SQLFunctionResources.getChiefCodeBySubordinateCodeSQL(), new Long[] { codes.get(0) });
            for (Long chiefCode : chiefsCodes) {
                if (directorsCodesList.contains(chiefCode)) {
                    result.add(chiefCode);
                    return result;
                }
                codes.add(chiefCode);
            }
            codes.remove(0);
        }
        throw new InternalApplicationException("Code hierarchy contains no director for actor with code = " + code);
    }

    private static List<Long> getCodesFromResultSet(ResultSet rs) throws SQLException {
        List<Long> codeList = Lists.newArrayList();
        while (rs.next()) {
            codeList.add(rs.getLong(1));
        }
        return codeList;
    }

    private static void setParameters(PreparedStatement ps, Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            ps.setObject(i + 1, parameters[i]);
        }
    }

    private static Context context;

    private static Context getInitialContext() {
        if (context == null) {
            try {
                context = new InitialContext();
            } catch (NamingException e) {
                throw Throwables.propagate(e);
            }
        }
        return context;
    }

    private static Connection getConnection() throws SQLException {
        try {
            DataSource ds = (DataSource) getInitialContext().lookup(SQLFunctionResources.getDataSourceName());
            if (ds == null) {
                throw new InternalApplicationException("No DataSource found for " + SQLFunctionResources.getDataSourceName());
            }
            return ds.getConnection();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
