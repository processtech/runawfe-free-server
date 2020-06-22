package ru.runa.common.web.tag;

import com.google.common.base.Throwables;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.action.ViewInternalStorageAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "viewSheets")
public class ViewSheetsTag extends TagSupport {
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
            try (InputStream is = new FileInputStream(workbookPath)) {
                Workbook wb = null;
                if (workbookPath.endsWith(".xls")) {
                    wb = new HSSFWorkbook(is);
                } else if (workbookPath.endsWith(".xlsx")) {
                    wb = new XSSFWorkbook(is);
                } else {
                    throw new IllegalArgumentException("excel file extension is incorrect");
                }
                for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                    Sheet sheet = wb.getSheetAt(i);
                    Map<String, String> params = new HashMap<>();
                    params.put("sheetName", sheet.getSheetName());
                    String href = Commons.getActionUrl(ViewInternalStorageAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
                    html.append("<a href=\"").append(href).append("\">").append(sheet.getSheetName()).append("</a>&nbsp;&nbsp;&nbsp;");
                }
                wb.close();
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
