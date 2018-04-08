package ru.runa.wfe.commons;

import java.util.Collection;
import java.util.HashSet;
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
}
