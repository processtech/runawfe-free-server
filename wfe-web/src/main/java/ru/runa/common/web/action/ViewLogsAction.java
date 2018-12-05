package ru.runa.common.web.action;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
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
    private static int autoReloadTimeoutSec = WebResources.getViewLogsAutoReloadTimeout();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        try {
            Delegates.getAuthorizationService().checkAllowed(Commons.getUser(request.getSession()), Permission.ALL, SecuredSingleton.LOGS);

            String logDirPath = IoCommons.getLogDirPath();
            request.setAttribute("logDirPath", logDirPath);
            ViewLogForm form = (ViewLogForm) actionForm;
            if (form.getFileName() != null) {
                File file = new File(logDirPath, form.getFileName());

                if (form.getMode() == ViewLogForm.MODE_DOWNLOAD) {
                    String encodedFileName = HTMLUtils.encodeFileName(request, form.getFileName());
                    response.setHeader("Content-disposition", "attachment; filename=\"" + encodedFileName + "\"");
                    OutputStream os = response.getOutputStream();
                    Files.copy(file, os);
                    os.flush();
                    return null;
                }

                int allLinesCount = countLines(file);
                form.setAllLinesCount(allLinesCount);

                if (form.getMode() == ViewLogForm.MODE_PAGING) {
                    request.setAttribute("pagingToolbar", createPagingToolbar(form));
                }

                String logFileContent = null;
                if (form.getMode() == ViewLogForm.MODE_SEARCH) {
                    logFileContent = wrapLines(searchLines(file, form, new ArrayList<>()), form, new ArrayList<>());
                } else if (form.getMode() == ViewLogForm.MODE_ERRORS) {
                    logFileContent = wrapLines(searchErrors(file, new ArrayList<>()), form, new ArrayList<>());
                } else if (form.getMode() == ViewLogForm.MODE_WARNS) {
                    logFileContent = wrapLines(searchWarns(file, new ArrayList<>()), form, new ArrayList<>());
                } else {
                    logFileContent = wrapLines(readLines(file, form), form, null);
                }
                request.setAttribute("logFileContent", logFileContent);
            }
            form.setLimitLinesCount(limitLinesCount);
            request.setAttribute("autoReloadTimeoutSec", autoReloadTimeoutSec);
            return mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
    }
    
    private String wrapLines (String lines, ViewLogForm form, List<Integer> lineNumbers) {
        StringBuilder b = new StringBuilder(lines.length() + 200);
        b.append("<table class=\"log\"><tr><td class=\"lineNumbers\">");
        if (lineNumbers != null) {
            for (Integer num : lineNumbers) {
                b.append(num).append("<br>");
            }
        } else {
            for (int i = form.getStartLine(); i <= form.getEndLine(); i++) {
                b.append(i).append("<br>");
            }
        }
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
                // count last line
                count++;
            }
            return count;
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    private String createPagingToolbar(ViewLogForm form) {
        if (form.getAllLinesCount() > limitLinesCount) {
            StringBuilder b = new StringBuilder();
            int n = form.getAllLinesCount() / limitLinesCount;
            if (form.getAllLinesCount() % limitLinesCount != 0) {
                n++;
            }
            for (int i = 0; i < n; i++) {
                int startFrom = i * limitLinesCount + 1;
                int endTo = startFrom + limitLinesCount - 1;
                String text;
                if (i == n - 1) {
                    text = "[" + startFrom + "-*]";
                } else {
                    text = "[" + startFrom + "-" + endTo + "]";
                }
                String href = "/wfe" + ViewLogsAction.ACTION_PATH + ".do?fileName=" + form.getFileName() + "&mode=1&startLine=" + startFrom
                        + "&endLine=" + endTo;
                b.append("<a href=\"").append(href).append("\">").append(text).append("</a>&nbsp;&nbsp;&nbsp;");
            }
            return b.toString();
        }
        return null;
    }

    private String readLines(File file, ViewLogForm form) throws IOException {
        if (form.getEndLine() - form.getStartLine() > limitLinesCount) {
            form.setEndLine(form.getStartLine() + limitLinesCount - 1);
        }
        int startLineNumber = form.getStartLine();
        int endLineNumber = form.getEndLine();
        InputStream is = null;
        LineNumberReader lnReader = null;
        try {
            int initialSize = (endLineNumber - startLineNumber) * 100;
            if (initialSize <= 0) {
                initialSize = 1000;
            }
            StringBuilder b = new StringBuilder(initialSize);
            is = new FileInputStream(file);
            lnReader = new LineNumberReader(new InputStreamReader(is));
            String line;
            while (null != (line = lnReader.readLine())) {
                if (lnReader.getLineNumber() >= startLineNumber) {
                    if (endLineNumber != 0 && lnReader.getLineNumber() > endLineNumber) {
                        break;
                    }
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                }
            }
            for (int i = lnReader.getLineNumber() + 1; i <= endLineNumber; i++) {
                b.append("<br>");
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(lnReader);
            Closeables.closeQuietly(is);
        }
    }

    private String searchLines(File file, ViewLogForm form, List<Integer> lineNumbers) throws IOException {
        InputStream is = null;
        LineNumberReader lnReader = null;
        try {
            StringBuilder b = new StringBuilder(1000);
            is = new FileInputStream(file);
            lnReader = new LineNumberReader(new InputStreamReader(is));
            int i = 1;
            String line;
            while (null != (line = lnReader.readLine())) {
                boolean result;
                if (form.isSearchCaseSensitive()) {
                    result = StringUtils.contains(line, form.getSearch());
                } else {
                    result = StringUtils.containsIgnoreCase(line, form.getSearch());
                }
                if (result) {
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                    lineNumbers.add(i);
                    if (lineNumbers.size() > limitLinesCount) {
                        break;
                    }
                }
                i++;
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(is);
            Closeables.closeQuietly(lnReader);
        }
    }

    private String searchErrors(File file, List<Integer> lineNumbers) throws IOException {
        InputStream is = null;
        LineNumberReader lnReader = null;
        try {
            // TODO may be use more structured parsing
            // http://logging.apache.org/log4j/companions/receivers/apidocs/org/apache/log4j/varia/LogFilePatternReceiver.html
            StringBuilder b = new StringBuilder(1000);
            is = new FileInputStream(file);
            lnReader = new LineNumberReader(new InputStreamReader(is));
            int i = 1;
            String line;
            boolean found = false;
            while (null != (line = lnReader.readLine())) {
                if (found && line.length() > 0 && (Character.isWhitespace(line.charAt(0)) || Character.isLetter(line.charAt(0)))) {
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                    lineNumbers.add(i);
                } else {
                    found = StringUtils.contains(line, " ERROR ");
                    if (found) {
                        line = StringEscapeUtils.escapeHtml(line);
                        line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                        b.append(line).append("<br>");
                        lineNumbers.add(i);
                        if (lineNumbers.size() > limitLinesCount) {
                            break;
                        }
                    }
                }
                i++;
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(lnReader);
            Closeables.closeQuietly(is);
        }
    }
    
    private String searchWarns(File file, List<Integer> lineNumbers) throws IOException {
        InputStream is = null;
        LineNumberReader lnReader = null;
        try {
            // TODO may be use more structured parsing
            // http://logging.apache.org/log4j/companions/receivers/apidocs/org/apache/log4j/varia/LogFilePatternReceiver.html
            StringBuilder b = new StringBuilder(1000);
            is = new FileInputStream(file);
            lnReader = new LineNumberReader(new InputStreamReader(is));
            int i = 1;
            String line;
            boolean found = false;
            while (null != (line = lnReader.readLine())) {
                if (found && line.length() > 0 && (Character.isWhitespace(line.charAt(0)) || Character.isLetter(line.charAt(0)))) {
                    line = StringEscapeUtils.escapeHtml(line);
                    line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                    b.append(line).append("<br>");
                    lineNumbers.add(i);
                } else {
                    found = StringUtils.contains(line, " WARN ");
                    if (found) {
                        line = StringEscapeUtils.escapeHtml(line);
                        line = line.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
                        b.append(line).append("<br>");
                        lineNumbers.add(i);
                        if (lineNumbers.size() > limitLinesCount) {
                            break;
                        }
                    }
                }
                i++;
            }
            return b.toString();
        } finally {
            Closeables.closeQuietly(lnReader);
            Closeables.closeQuietly(is);
        }
    }
}
