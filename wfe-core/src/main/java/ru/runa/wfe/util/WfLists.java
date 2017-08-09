package ru.runa.wfe.util;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author vasilievda
 * @since 08.08.2017
 */
public final class WfLists {
    private WfLists() {
    }

    /**
     * {@link Lists#partition(java.util.List, int)}
     */
    public static <T> List<List<T>> partition(Set<T> set, int size) {
        return partition(Lists.newArrayList(set), size);
    }

    /**
     * {@link Lists#partition(java.util.List, int)}
     */
    public static <T> List<List<T>> partition(List<T> list, int size) {
        if (list.size() < size) {
            ArrayList<List<T>> out = Lists.newArrayList();
            out.add(list);
            return out;
        }
        return Lists.partition(list, size);
    }
}