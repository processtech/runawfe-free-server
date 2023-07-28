package ru.runa.wfe.presentation.hibernate;

import java.util.Collection;
import org.hibernate.Query;
import org.hibernate.type.Type;

public class QueryParameterValue {
    private final Object value;
    private final Type type;
    private final boolean isList;

    /**
     * @param type May be null.
     * @param isList If true, setParameterList() will be called on query instead of setParameter().
     */
    QueryParameterValue(Object value, Type type, boolean isList) {
        this.value = value;
        this.type = type;
        this.isList = isList;
    }

    public void apply(Query q, String name) {
        if (isList) {
            if (type != null) {
                if (value instanceof Collection<?>) {
                    q.setParameterList(name, (Collection<?>)value, type);
                } else {
                    q.setParameterList(name, (Object[])value, type);
                }
            } else {
                if (value instanceof Collection<?>) {
                    q.setParameterList(name, (Collection<?>)value);
                } else {
                    q.setParameterList(name, (Object[])value);
                }
            }
        } else {
            if (type != null) {
                q.setParameter(name, value, type);
            } else {
                q.setParameter(name, value);
            }
        }
    }
}
