package ru.runa.common.web.tag;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.action.ViewLogsAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "viewLogs")
public class ViewLogsTag extends TagSupport {
    private static final long serialVersionUID = 1L;
    private String logDirPath;

    public String getLogDirPath() {
        return logDirPath;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setLogDirPath(String logDirPath) {
        this.logDirPath = logDirPath;
    }

    @Override
    public int doStartTag() {
        try {
            StringBuilder html = new StringBuilder();
            File dirFile = new File(logDirPath);
            if (dirFile.exists() && dirFile.isDirectory()) {
                if (Delegates.getAuthorizationService().isAllowed(getUser(), SystemPermission.VIEW_LOGS, ASystem.INSTANCE)) {
                    File[] logFiles = dirFile.listFiles();
                    Arrays.sort(logFiles);
                    for (File file : logFiles) {
                        if (file.isFile()) {
                            long kiloBytes = file.length() / 1024;
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("fileName", file.getName());
                            String href = Commons.getActionUrl(ViewLogsAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
                            html.append("<a href=\"").append(href).append("\">").append(file.getName()).append(" (").append(kiloBytes).append("KB)</a>&nbsp;&nbsp;&nbsp;");
                        }
                    }
                } else {
                    html.append("<ul>");
                    for (File file : dirFile.listFiles()) {
                        html.append("<li>").append(file.getName()).append("</li>");
                    }
                    html.append("</ul>");
                }
            } else {
                html.append("unknown ").append(logDirPath);
            }
            pageContext.getOut().write(html.toString());
            return Tag.SKIP_BODY;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private User getUser() {
        return Commons.getUser(pageContext.getSession());
    }

}
