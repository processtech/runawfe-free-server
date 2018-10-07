/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.commons.hibernate;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashMap;
import lombok.val;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

@SuppressWarnings("WeakerAccess")
public abstract class DbAwareEnumType<T, E extends DbAwareEnum<T>> implements UserType {

    private int[] types;
    private Class<T> sqlClass;
    private Class<?> enumClass;
    private HashMap<T, E> registry;

    /**
     *
     * @param sqlType java.sql.Types.INTEGER, VARCHAR, etc.
     * @param sqlClass Integer.class, String.class, etc. Must correspond to sqlType.
     */
    @SuppressWarnings("unchecked")
    protected DbAwareEnumType(int sqlType, Class<T> sqlClass, Class<E> enumClass) {
        Preconditions.checkArgument(Enum.class.isAssignableFrom(enumClass));
        this.types = new int[] { sqlType };
        this.sqlClass = sqlClass;
        this.enumClass = enumClass;
        EnumSet items = EnumSet.allOf((Class<? extends Enum>) this.enumClass);
        registry = new HashMap<>(items.size());
        for (val ei : items) {
            val dei = (E)ei;
            registry.put(dei.getDbValue(), dei);
        }
    }

    @Override
    public boolean equals(Object o1, Object o2) {
        return (o1 == o2);
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        return o.hashCode();
    }

    @Override
    public Object deepCopy(Object o) throws HibernateException {
        return o;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object o) throws HibernateException {
        return (Serializable) o;
    }

    @Override
    public Object assemble(Serializable s, Object o) throws HibernateException {
        return s;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return target;
    }

    @Override
    public int[] sqlTypes() {
        return types;
    }

    @Override
    public Class<?> returnedClass() {
        return enumClass;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        val o = rs.getObject(names[0], sqlClass);
        if (o == null) {
            return null;
        }
        val ei = registry.get(o);
        if (ei == null) {
            throw new RuntimeException("Unknown DB value '" + o + "' for enum " + enumClass.getCanonicalName());
        }
        return ei;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, types[0]);
        } else {
            st.setObject(index, ((E)value).getDbValue(), types[0]);
        }
    }
}
