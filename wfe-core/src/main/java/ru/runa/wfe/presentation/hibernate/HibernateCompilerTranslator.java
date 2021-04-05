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
package ru.runa.wfe.presentation.hibernate;

import java.util.HashMap;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.hql.classic.QueryTranslatorImpl;

import ru.runa.wfe.commons.ApplicationContextFactory;

/**
 * Contains functions to work with {@link QueryTranslatorImpl}, used to convert HQL query to SQL query.
 */
public class HibernateCompilerTranslator {

    /**
     * Translated HQL query.
     */
    private final String hqlQuery;

    /**
     * Flag, equals true, if this query is for object count; false otherwise.
     */
    private final boolean isCountQuery;

    /**
     * Object, used to translate HQL to SQL query.
     */
    private QueryTranslatorImpl translator;

    /**
     * Creates class to translate HQL to SQL query.
     * 
     * @param hqlQuery
     *            HQL query to translate.
     * @param isCountQuery
     *            Flag, equals true, if this query is for object count; false otherwise.
     */
    public HibernateCompilerTranslator(String hqlQuery, boolean isCountQuery) {
        this.hqlQuery = hqlQuery;
        this.isCountQuery = isCountQuery;
    }

    /**
     * Translates HQL query to SQL query.
     * 
     * @return Translated SQL query.
     */
    public String translate() {
        if (translator != null) {
            return translator.getSQLString();
        }
        translator = new QueryTranslatorImpl(hqlQuery, new HashMap<String, Object>(),
                (SessionFactoryImplementor) ApplicationContextFactory.getSessionFactory());
        translator.compile(new HashMap<String, Object>(), isCountQuery);
        return translator.getSQLString();
    }

    /**
     * Returns alias in SQL query for alias is HQL query.
     * 
     * @param alias
     *            Alias name from HQL query.
     * @return Alias name from SQL query.
     */
    public String getAliasName(String alias) {
        return translator.getAliasName(alias);
    }
}
