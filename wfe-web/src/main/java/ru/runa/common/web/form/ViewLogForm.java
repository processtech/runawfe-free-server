package ru.runa.common.web.form;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author dofs
 *
 * @struts:form name = "viewLogForm"
 */
public class ViewLogForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    public static final int MODE_READBEGIN = 1;
    public static final int MODE_READEND = 2;
    public static final int MODE_DOWNLOAD = 3;

    private String fileName;
    private int allLinesCount;
    private int mode = MODE_READEND;
    private int startLine = 1;
    private int endLine = 0;
    private int endLines = 500;
    private int limitLinesCount;
    private int linesFound;
    private int limitLineCharactersCount;
    private boolean autoReload;
    private String search;
    private boolean searchCaseSensitive;
    private boolean searchContainsWord;
    private boolean configureLineCharactersCount;
    private boolean searchErrors;
    private boolean searchWarns;

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        autoReload = false;
        searchCaseSensitive = false;
        configureLineCharactersCount = false;
        searchContainsWord = false;
        searchErrors = false;
        searchWarns = false;
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
        if (endLine == 0) {
            endLine = allLinesCount;
        }

        if (endLine > allLinesCount) {
            endLine = allLinesCount;
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

    public boolean isSearchContainsWord() {
        return searchContainsWord;
    }

    public void setSearchContainsWord(boolean searchContainsWord) {
        this.searchContainsWord = searchContainsWord;
    }

    public boolean isSearchErrors() {
        return searchErrors;
    }

    public void setSearchErrors(boolean searchErrors) {
        this.searchErrors = searchErrors;
    }

    public boolean isSearchWarns() {
        return searchWarns;
    }

    public void setSearchWarns(boolean searchWarns) {
        this.searchWarns = searchWarns;
    }

    public int getLinesFound() {
        return linesFound;
    }

    public void setLinesFound(int linesFound) {
        this.linesFound = linesFound;
    }

    public int getLimitLineCharactersCount() {
        return limitLineCharactersCount;
    }

    public void setLimitLineCharactersCount(int limitLineCharactersCount) {
        this.limitLineCharactersCount = limitLineCharactersCount;
    }

    public boolean isConfigureLineCharactersCount() {
        return configureLineCharactersCount;
    }

    public void setConfigureLineCharactersCount(boolean configureLineCharactersCount) {
        this.configureLineCharactersCount = configureLineCharactersCount;
    }
}
