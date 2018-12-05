package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author dofs
 *
 * @struts:form name = "viewLogForm"
 */
public class ViewLogForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    public static final int MODE_PAGING = 1;
    public static final int MODE_SEARCH = 2;
    public static final int MODE_END_LINES = 3;
    public static final int MODE_ERRORS = 4;
    public static final int MODE_DOWNLOAD = 5;
    public static final int MODE_WARNS = 6;

    private String fileName;
    private int allLinesCount;
    private int mode = MODE_END_LINES;
    private int startLine = 1;
    private int endLine = 0;
    private int endLines = 500;
    private int limitLinesCount;
    private boolean autoReload;
    private String search;
    private boolean searchCaseSensitive;

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        autoReload = false;
        searchCaseSensitive = false;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAllLinesCount() {
        return allLinesCount;
    }

    public void setAllLinesCount(int allLinesCount) {
        this.allLinesCount = allLinesCount;
        if (mode == ViewLogForm.MODE_END_LINES) {
            startLine = allLinesCount - endLines;
            endLine = allLinesCount;
        } else if (mode == ViewLogForm.MODE_PAGING) {
            if (endLine == 0) {
                endLine = allLinesCount;
            }
            if (endLine > allLinesCount) {
                endLine = allLinesCount;
            }
        }
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndLines() {
        return endLines;
    }

    public void setEndLines(int endLines) {
        this.endLines = endLines;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public void setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
    }

    public int getLimitLinesCount() {
        return limitLinesCount;
    }

    public void setLimitLinesCount(int limitLinesCount) {
        this.limitLinesCount = limitLinesCount;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public boolean isSearchCaseSensitive() {
        return searchCaseSensitive;
    }

    public void setSearchCaseSensitive(boolean searchCaseSensitive) {
        this.searchCaseSensitive = searchCaseSensitive;
    }
}
