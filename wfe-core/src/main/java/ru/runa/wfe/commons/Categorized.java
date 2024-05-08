package ru.runa.wfe.commons;

public interface Categorized {

    public String getCategory();

    public default String[] getCategories() {
        if (getCategory() != null) {
            return getCategory().split(Utils.CATEGORY_DELIMITER);
        }
        return new String[] {};
    }

}
