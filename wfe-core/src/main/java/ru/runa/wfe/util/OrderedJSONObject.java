package ru.runa.wfe.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

public class OrderedJSONObject extends JSONObject {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private final Set entries = new ArraySet();

    @SuppressWarnings("rawtypes")
    @Override
    public final Object get(Object key) {
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (!entry.getKey().equals(key)) {
                continue;
            }
            return entry.getValue();
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public final Object put(Object key, Object value) {
        entries.add(new Entry(key, value));
        return value;
    }

    @SuppressWarnings({ "rawtypes" })
    @Override
    public final Object remove(Object key) {
        Iterator iter = entries.iterator();
        Map.Entry entry = null;
        Object result = null;
        while (iter.hasNext()) {
            entry = (Map.Entry) iter.next();
            if (!entry.getKey().equals(key)) {
                entry = null;
                continue;
            }
            result = entry.getValue();
            break;
        }
        if (entry != null) {
            entries.remove(entry);
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final void putAll(Map m) {
        Iterator iter = m.entrySet().iterator();
        while (iter.hasNext()) {
            entries.add(iter.next());
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public final boolean containsKey(Object key) {
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (!entry.getKey().equals(key)) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final boolean containsValue(Object value) {
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (!entry.getValue().equals(value)) {
                continue;
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final Set keySet() {
        Set result = new ArraySet();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            result.add(entry.getKey());
        }
        return result;
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public final Set entrySet() {
        return entries;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection values() {
        Collection result = Collections.emptyList();
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            result.add(entry.getValue());
        }
        return result;
    }

    static class Entry<K, V> implements Map.Entry<K, V> {

        K key;
        V value;

        Entry(K k, V v) {
            key = k;
            setValue(v);
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            this.value = value;
            return value;
        }

    }
}
