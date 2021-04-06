package ru.runa.wfe.commons.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.var.Converter;

/**
 * is the hibernate UserType for storing converters as a char in the database. The conversion can be found (and customized) in the file jbpm.converter.properties.
 */
public class ConverterEnumType implements UserType {

    static final int[] SQLTYPES = new int[] { Types.CHAR };

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
        return SQLTYPES;
    }

    @Override
    public Class<?> returnedClass() {
        return Converter.class;
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String converterDatabaseId = resultSet.getString(names[0]);
        return ApplicationContextFactory.getConverters().getConverterByDatabaseId(converterDatabaseId);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index, SessionImplementor session)
            throws HibernateException, SQLException {
        String converterDatabaseId = ApplicationContextFactory.getConverters().getConverterId((Converter) value);
        preparedStatement.setString(index, converterDatabaseId);
    }

}
