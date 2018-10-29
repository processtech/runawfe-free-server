package ru.runa.wfe.presentation.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.type.Type;

public class QueryParametersMap {
    private final HashMap<String, QueryParameterValue> map = new HashMap<>();

    public Set<String> getNames() {
        return map.keySet();
    }

    public void add(String name, String value) {
        map.put(name, new QueryParameterValue(value, Hibernate.STRING, false));
    }

    public void add(String name, Long value) {
        map.put(name, new QueryParameterValue(value, Hibernate.LONG, false));
    }

    public void add(String name, Date value) {
        map.put(name, new QueryParameterValue(value, Hibernate.DATE, false));
    }

    public void add(String name, Collection<?> value) {
        map.put(name, new QueryParameterValue(value, null, true));
    }

    public void add(String name, Collection<?> value, Type type) {
        map.put(name, new QueryParameterValue(value, type, true));
    }

    public void apply(Query q) {
        for (Map.Entry<String, QueryParameterValue> kv : map.entrySet()) {
            kv.getValue().apply(q, kv.getKey());
        }
    }
}
