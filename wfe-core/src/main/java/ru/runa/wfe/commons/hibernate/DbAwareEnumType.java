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

/**
 * Don't delete, maybe someday we'll optimize DB row sizes.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
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
