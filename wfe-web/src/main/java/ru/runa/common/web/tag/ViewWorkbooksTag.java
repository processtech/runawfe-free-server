package ru.runa.common.web.tag;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.io.FilenameUtils;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.action.ViewInternalStorageAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "viewWorkbooks")
public class ViewWorkbooksTag extends TagSupport {
    private static final long serialVersionUID = 1L;
    private String workbookPath;

    public String getworkbookPath() {
        return workbookPath;
    }

    @Attribute(required = true)
    public void setworkbookPath(String workbookPath) {
        this.workbookPath = workbookPath;
    }

    @Override
    public int doStartTag() {
        if (!Delegates.getExecutorService().isAdministrator(getUser())) {
            throw new AuthorizationException("No permission on this page");
        }
        try {
            StringBuilder html = new StringBuilder();
            File internalStorage = new File(workbookPath);
            if (internalStorage.exists() && internalStorage.isDirectory()) {
                String[] workbookNameList = internalStorage.list((dir, name) -> {
                    return name.endsWith(".xls") || name.endsWith(".xlsx");
                });
                for (int i = 0; i < workbookNameList.length; i++) {
                    Map<String, String> params = new HashMap<>();
                    params.put("workbookName", workbookNameList[i]);
                    String href = Commons.getActionUrl(ViewInternalStorageAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
                    html.append("<a href=\"").append(href).append("\">").append(FilenameUtils.removeExtension(workbookNameList[i]))
                            .append("</a>&nbsp;&nbsp;&nbsp;");
                }
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
