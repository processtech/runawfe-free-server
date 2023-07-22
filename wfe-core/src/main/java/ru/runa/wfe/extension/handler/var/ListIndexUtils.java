package ru.runa.wfe.extension.handler.var;

import ru.runa.wfe.commons.SystemProperties;

public class ListIndexUtils {

    /**
     * Back compatibility issue
     */
    public static Integer adjustIndex(Integer index) {
        if (index == null) {
            return null;
        }
        if (!SystemProperties.getResources().getBooleanProperty("handler.list.indexes.starts.with.zero", false)) {
            if (index == 0) {
                throw new RuntimeException("Since v4.2.0 indexes start with 1.");
            }
            index--;
        }
        if (index < 0) {
            throw new RuntimeException("Negative indexes do not allowed.");
        }
        return index;
    }

}
