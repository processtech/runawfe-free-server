package ru.runa.wf.logic.bot.mswordreport;

public class BookmarkVariableMapping {
    private final String bookmarkName;
    private final String variableName;

    public BookmarkVariableMapping(String bookmarkName, String variableName) {
        this.bookmarkName = bookmarkName;
        this.variableName = variableName;
    }

    public String getBookmarkName() {
        return bookmarkName;
    }

    public String getVariableName() {
        return variableName;
    }

}
