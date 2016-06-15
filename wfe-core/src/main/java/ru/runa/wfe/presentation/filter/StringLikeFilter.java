package ru.runa.wfe.presentation.filter;

public class StringLikeFilter {
    private final String searchFilter;
    private final boolean useLike;

    public StringLikeFilter(String searchFilter, boolean useLike) {
        super();
        this.searchFilter = searchFilter;
        this.useLike = useLike;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public boolean isUseLike() {
        return useLike;
    }
}
