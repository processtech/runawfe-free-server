package ru.runa.wfe.commons.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;

public abstract class ExtensibleEnumType implements EnhancedUserType {

    private static int[] SQLTYPES = new int[] { Types.VARCHAR };

    @Override
    public int[] sqlTypes() {
        return SQLTYPES;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable)value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        String s = rs.getString(names[0]);
        // I use fromXMLString() here because it does what I need, to avoid introducing another correctly named abstract method.
        return s == null ? null : fromXMLString(s);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            // I use toXMLString() here because it does what I need, to avoid introducing another correctly named abstract method.
            st.setObject(index, toXMLString(value), Types.VARCHAR);
        }
    }

    @Override
    public String objectToSQLString(Object value) {
        return '\'' + toXMLString(value) + '\'';
    }
}
