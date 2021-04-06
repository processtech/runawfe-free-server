package ru.runa.common.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class CategoriesIterator implements Iterator<String[]> {

    private final List<String[]> categories = new ArrayList<String[]>();

    private int curIdx = 0;

    public CategoriesIterator(List<String[]> types) {
        SortedSet<String[]> categoriesSet = new TreeSet<String[]>(new Comparator<String[]>() {

            @Override
            public int compare(String[] o1, String[] o2) {
                int length = o1.length > o2.length ? o2.length : o1.length;
                for (int i = 0; i < length; ++i) {
                    int compareResult = o1[i].compareTo(o2[i]);
                    if (compareResult == 0) {
                        continue;
                    }
                    return compareResult;
                }
                if (o1.length > length) {
                    return 1;
                }
                if (o2.length > length) {
                    return -1;
                }
                return 0;
            }

        });
        for (String[] type : types) {
            for (int i = 0; i < type.length; ++i) {
                String[] subType = new String[i + 1];
                for (int st = 0; st <= i; ++st) {
                    subType[st] = type[st];
                }
                categoriesSet.add(subType);
            }
        }
        categories.addAll(categoriesSet);
    }

    @Override
    public boolean hasNext() {
        return curIdx < categories.size();
    }

    @Override
    public String[] next() {
        return categories.get(curIdx++);
    }

    public String[] getItem(int idx) {
        return categories.get(idx);
    }

    @Override
    public void remove() {
    }
}
