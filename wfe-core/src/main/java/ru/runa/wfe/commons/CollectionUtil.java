package ru.runa.wfe.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CollectionUtil {

    /**
     * Returns union of two collections as set.
     */
    public static <T> Set<T> unionSet(Collection<T> a, Collection<T> b) {
        Set<T> set = new HashSet<>(a.size() + b.size());
        set.addAll(a);
        set.addAll(b);
        return set;
    }

    /**
     * Returns difference of two collections as set.
     */
    public static <T> Set<T> diffSet(Collection<T> a, Collection<T> b) {
        Set<T> set = new HashSet<>(a);
        set.removeAll(b);
        return set;
    }

    /**
     * Returns difference of two collections as list.
     * Use if you want to get elements in the same order as in first collection, or just in any deterministic order.
     */
    public static <T> ArrayList<T> diffList(Collection<T> a, Set<T> b) {
        ArrayList<T> list = new ArrayList<>(a.size());
        for (T x : a) {
            if (!b.contains(x)) {
                list.add(x);
            }
        }
        return list;
    }

    /**
     * Map.putIfAbsent() returns null if key previously didn't exist. I want it to return default value.
     */
    public static <K,V> V mapGetOrPutDefault(Map<K, V> map, K key, V value) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            map.put(key, value);
            return value;
        }
    }
}
