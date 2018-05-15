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
package ru.runa.wfe.commons.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SQLQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * DAO for database initialization and variables managing. Creates appropriate
 * tables (drops tables if such tables already exists) and records.
 */
@Component
public class ConstantDAO extends GenericDAO<Constant> {
    private static final Log log = LogFactory.getLog(ConstantDAO.class);
    private static final String DATABASE_VERSION_VARIABLE_NAME = "ru.runa.database_version";

    @Transactional
    public Integer getDatabaseVersion() throws Exception {
        org.hibernate.classic.Session session = sessionFactory.getCurrentSession();
        Connection connection = session.connection();
        DatabaseMetaData metaData = connection.getMetaData();
        log.info("Running with " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
        SQLQuery query = session.createSQLQuery("SELECT VALUE FROM WFE_CONSTANTS WHERE NAME=:name");
        query.setString("name", DATABASE_VERSION_VARIABLE_NAME);
        return TypeConversionUtil.convertTo(Integer.class, query.uniqueResult());
    }

    public void setDatabaseVersion(int version) {
        setValue(DATABASE_VERSION_VARIABLE_NAME, String.valueOf(version));
    }

    public Constant get(String name) {
        QConstant c = QConstant.constant;
        return queryFactory.selectFrom(c).where(c.name.eq(name)).fetchFirst();
    }

    /**
     * Load constant value. Returns null, if constant is not present.
     * 
     * @param name
     *            constant name.
     * @return constant value.
     */
    public String getValue(String name) {
        Constant constant = get(name);
        if (constant == null) {
            return null;
        }
        return constant.getValue();
    }

    /**
     * Save constant.
     * 
     * @param name
     *            constant name.
     * @param value
     *            constant value.
     */
    public void setValue(String name, String value) {
        Constant constant = get(name);
        if (constant == null) {
            create(new Constant(name, value));
        } else {
            constant.setValue(value);
        }
    }
}
