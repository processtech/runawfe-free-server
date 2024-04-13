package ru.runa.common.web.action;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.ViewLogForm;
import ru.runa.wfe.commons.IoCommons;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author dofs
 * @struts:action path="/viewLogs" name="viewLogForm" validate="false"
 * @struts.action-forward name="success" path="/displayLogs.do" redirect =
 * "false"
 */
public class ViewLogsAction extends ActionBase {
    public static final String ACTION_PATH = "/viewLogs";
    private static int limitLinesCount = WebResources.getViewLogsLimitLinesCount();
    private static int limitLineCharactersCount = WebResources.getViewLogsLimitLineCharactersCount();
    private static int autoReloadTimeoutSec = WebResources.getViewLogsAutoReloadTimeout();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            Delegates.getAuthorizationService().checkAllowed(getLoggedUser(request), Permission.VIEW_LOGS, SecuredSingleton.SYSTEM);

            String logDirPath = IoCommons.getLogDirPath();
            request.setAttribute("logDirPath", logDirPath);
            ViewLogForm form = (ViewLogForm) actionForm;
            String fileName = form.getFileName();
            if (fileName != null) {
                File file = new File(logDirPath, fileName);
                String canonicalPath = file.getCanonicalPath();
                if (!canonicalPath.contains(logDirPath)) {
                    FileNotFoundException fnf = new FileNotFoundException("File not found " + file);
                    addError(request, fnf);
                    return mapping.findForward(Resources.FORWARD_FAILURE);
                }

                if (form.getMode() == ViewLogForm.MODE_DOWNLOAD) {
                    String encodedFileName = HTMLUtils.encodeFileName(request, fileName);
                    response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
                    OutputStream os = response.getOutputStream();
                    Files.copy(file, os);
                    os.flush();
                    return null;
                }

                if (form.getLimitLinesCount() == 0) {
                    form.setLimitLinesCount(limitLinesCount);
                }

                if (form.getLimitLineCharactersCount() == 0) {
                    form.setLimitLineCharactersCount(limitLineCharactersCount);
                }

                if (form.getEndLine() == 0) {
                    form.setEndLine(form.getLimitLinesCount());
                }

                String logFileContent;
                if (form.getMode() == ViewLogForm.MODE_READBEGIN) {
                    logFileContent = wrapLines(searchLines(file, form), form);
                } else {
                    logFileContent = wrapLines(searchLinesReverse(file, form), form);
                }
                request.setAttribute("logFileContent", logFileContent);
                int allLinesCount = countLines(file);
                form.setAllLinesCount(allLinesCount);

                request.setAttribute("pagingToolbar", createPagingToolbar(form));
            }

            form.setLimitLinesCount(form.getLimitLinesCount() == 0 ? limitLinesCount : form.getLimitLinesCount());
            request.setAttribute("autoReloadTimeoutSec", autoReloadTimeoutSec);
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }

    private String wrapLines(String lines, ViewLogForm form) {
        StringBuilder b = new StringBuilder(lines.length() + 200);
        b.append("<table class=\"log\"><tr><td class=\"lineNumbers\">");
        b.append("</td><td class=\"content\">");
        b.append(lines);
        b.append("</td></tr></table>");
        return b.toString();
    }

    private int countLines(File file) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            while ((readChars = is.read(c)) != -1) {
                for (int i = 0; i < readChars; i++) {
                    if (c[i] == '\n') {
                        count++;
                    }
                }
            }
            if (count > 0) {
                count++;
            }
            return count;
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private String createPagingToolbar(ViewLogForm form) {
        if (form.getLinesFound() > form.getLimitLinesCount()) {
            StringBuilder b = new StringBuilder();
            int n = form.getLinesFound() / form.getLimitLinesCount();
            if (form.getLinesFound() % form.getLimitLinesCount() != 0) {
                n++;
            }

            String href = getHrefPage(form, form.getStartLine() - form.getLimitLinesCount(), form.getEndLine() - form.getLimitLinesCount());
            b.append("<div class='paging-div' style='display: block;'><div style='display: inline-block;'><a style='font-weight: bold;' href=\"").append(href).append("\">").append("[ < Назад ]").append("</a></div>&nbsp;&nbsp;&nbsp;");

            href = getHrefPage(form, form.getStartLine() + form.getLimitLinesCount(), form.getEndLine() + form.getLimitLinesCount());
            b.append("<div style='display: inline-block'><a style='font-weight: bold;' href=\"").append(href).append("\">").append("[ Вперед > ]").append("</a></div>&nbsp;&nbsp;&nbsp;");

            for (int i = 0; i < n; i++) {
                int startFrom = i * form.getLimitLinesCount() + 1;
                int endTo = startFrom + form.getLimitLinesCount() - 1;
                String text;
                if (i == n - 1) {
                    text = "[" + startFrom + "-*]";
                } else {
                    text = "[" + startFrom + "-" + endTo + "]";
                }
                href = getHrefPage(form, startFrom, endTo);
                b.append("<div style='display: inline-block;'><a ").append(form.getStartLine() == startFrom ? "style=\"color:#3c0148; text-decoration:none\" class=\"current-page\" " : "").append(
                        "href=\"").append(href).append("\">").append(text).append("</a></div>&nbsp;&nbsp;&nbsp;");
            }

            b.append("</div>");
            return b.toString();
        }
        return null;
    }

    private String getHrefPage(ViewLogForm form, Integer startFrom, Integer endTo) {
        return "/wfe" + ViewLogsAction.ACTION_PATH +
                ".do?fileName=" + form.getFileName() +
                "&mode=" + form.getMode() +
                "&startLine=" + startFrom +
                "&endLine=" + endTo +
                "&searchContainsWord=" + form.isSearchContainsWord() +
                "&searchCaseSensitive=" + form.isSearchCaseSensitive() +
                "&searchErrors=" + form.isSearchErrors() +
                "&searchWarns=" + form.isSearchWarns() +
                "&limitLinesCount=" + form.getLimitLinesCount() +
                "&configureLineCharactersCount=" + form.isConfigureLineCharactersCount() +
                "&limitLineCharactersCount=" + form.getLimitLineCharactersCount() +
                "&search=" + (form.getSearch() == null ? "" : form.getSearch());
    }

    private String searchLines(File file, ViewLogForm form) throws IOException {
        try (LineNumberReader lReader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)))) {
            return search(form, lReader, false);
        }
    }

    private String searchLinesReverse(File file, ViewLogForm form) throws IOException {
        try (ReversedLinesFileReader rReader = new ReversedLinesFileReader(file)) {
            return search(form, rReader, true);
        }
    }

    private String search(ViewLogForm form, Object reader, boolean isReverse) throws IOException {
        StringBuilder pageLines = new StringBuilder(1000);
        String line;
        List<String> stackTraceLines;
        int linesFound = 0;
        boolean isLineWithStackTrace;
        boolean isStackTraceLine;
        String previouslyReadedLine = null;
        while (true) {
            if (previouslyReadedLine != null) {
                line = previouslyReadedLine;
                previouslyReadedLine = null;
            } else {
                line = isReverse ? ((ReversedLinesFileReader) reader).readLine() : ((LineNumberReader) reader).readLine();
                if (line == null) {
                    break;
                }
            }
            boolean result = isTrueResult(form, line);
            isStackTraceLine = isStackTrace(line);
            stackTraceLines = new ArrayList<>();
            isLineWithStackTrace = false;
            if (isReverse) {
                if (isStackTraceLine) {
                    boolean searchResultInThisRecord = result;
                    stackTraceLines.add(line);
                    String stackTraceLine;
                    while (((stackTraceLine = ((ReversedLinesFileReader) reader).readLine()) != null) && isStackTrace(stackTraceLine)) {
                        searchResultInThisRecord = searchResultInThisRecord || isTrueResult(form, stackTraceLine);
                        stackTraceLines.add(stackTraceLine);
                    }
                    if (stackTraceLine != null) {
                        result = isTrueResult(form, stackTraceLine);
                        searchResultInThisRecord = searchResultInThisRecord || result;
                        if (searchResultInThisRecord) {
                            stackTraceLines.add(stackTraceLine);
                            isLineWithStackTrace = true;
                        }
                    }
                    line = "";
                }
            } else {
                boolean searchResultInThisRecord = result;
                stackTraceLines.add(line);
                String stackTraceLine;
                while (((stackTraceLine = ((LineNumberReader) reader).readLine()) != null) && isStackTrace(stackTraceLine)) {
                    searchResultInThisRecord = searchResultInThisRecord || isTrueResult(form, stackTraceLine);
                    stackTraceLines.add(stackTraceLine);
                }
                if (stackTraceLine != null) { // first line from next record
                    previouslyReadedLine = stackTraceLine;
                }
                isLineWithStackTrace = searchResultInThisRecord;
            }

            if (result || isLineWithStackTrace) {
                linesFound++;
                if (linesFound < form.getStartLine()) {
                    continue;
                }
                if (linesFound <= form.getEndLine()) {
                    if (isLineWithStackTrace) {
                        boolean isStackTrace = false;
                        for (String stackTraceLine : stackTraceLines) {
                            addLine(stackTraceLine, isStackTrace, form, pageLines, isReverse);
                            isStackTrace = true;
                        }
                    } else {
                        addLine(line, false, form, pageLines, isReverse);
                    }
                }
            }
        }

        form.setLinesFound(linesFound);
        return pageLines.toString();
    }

    private boolean isTrueResult(ViewLogForm form, String line) {
        boolean isTrue = true;
        if (form.isSearchContainsWord() && !form.getSearch().isEmpty()) {
            isTrue = form.isSearchCaseSensitive() ? StringUtils.contains(line, form.getSearch()) : StringUtils.containsIgnoreCase(line, form.getSearch());
        }
        if (form.isSearchErrors() || form.isSearchWarns()) {
            isTrue = isTrue && isErrorsOrWarns(form, line);
        }
        return isTrue;
    }

    private boolean isErrorsOrWarns(ViewLogForm form, String line) {
        boolean result = false;
        if (form.isSearchErrors() && form.isSearchWarns()) {
            result = StringUtils.contains(line, " ERROR ") || StringUtils.contains(line, " WARN ");
        } else {
            if (form.isSearchErrors()) {
                result = StringUtils.contains(line, " ERROR ");
            }
            if (form.isSearchWarns()) {
                result = StringUtils.contains(line, " WARN ");
            }
        }
        return result;
    }

    private boolean isStackTrace(String line) {
        return (line.length() > 0 && (Character.isWhitespace(line.charAt(0)) || Character.isLetter(line.charAt(0)))) || line.length() == 0;
    }

    private void addLine(String line, boolean isStackTrace, ViewLogForm form, StringBuilder pageLines, boolean isReverse) {
        line = StringEscapeUtils.escapeHtml(line);
        line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        if (form.getSearch() != null && !form.getSearch().equals("")) {
            line = form.isSearchCaseSensitive() ? line.replaceAll(form.getSearch(), "<b>" + form.getSearch() + "</b>")
                    : replaceWithBoldIgnoreCase(line, form.getSearch());
        }
        if (line.length() > form.getLimitLineCharactersCount()) {
            line = configureLine(line, isStackTrace, form.getLimitLineCharactersCount());
        }
        if (isReverse) {
            pageLines.insert(0, line + "<br>");
        } else {
            pageLines.append(line).append("<br>");
        }
    }

    private String replaceWithBoldIgnoreCase(String text, String searchString) {
        int searchStringLength = searchString.length();
        StringBuilder result = new StringBuilder();
        int beginSearchWithIndex = 0;
        while (true) {
            int searchStringBeginIndex = StringUtils.indexOfIgnoreCase(text, searchString, beginSearchWithIndex);
            if (searchStringBeginIndex == -1) { // if the string was not found
                result.append(text, beginSearchWithIndex, text.length());
                break;
            }
            int searchStringEndIndex = searchStringBeginIndex + searchStringLength;
            result.append(text, beginSearchWithIndex, searchStringBeginIndex);
            result.append("<b>");
            result.append(text, searchStringBeginIndex, searchStringEndIndex);
            result.append("</b>");
            beginSearchWithIndex = searchStringEndIndex;
        }
        return result.toString();
    }

    private String configureLine(String line, boolean isStackTrace, int limitLineCharactersCount) {
        if (StringEscapeUtils.unescapeHtml(line).length() <= limitLineCharactersCount) {
            return line;
        }
        StringBuilder resultLine = new StringBuilder();
        while (line.length() > limitLineCharactersCount) {
            int finalSplitIndex = line.substring(0, limitLineCharactersCount).lastIndexOf(" ") + 1;
            char[] arrayChars = line.toCharArray();
            while (finalSplitIndex < arrayChars.length && arrayChars[finalSplitIndex] != ' ') {
                finalSplitIndex++;
            }
            resultLine.append(line.substring(0, finalSplitIndex));
            line = line.substring(finalSplitIndex);
            if (!line.isEmpty()) {
                resultLine.append("<br>");
                if (line.length() < limitLineCharactersCount) {
                    resultLine.append(line);
                }
            }
        }
        return resultLine.toString();
    }
}
