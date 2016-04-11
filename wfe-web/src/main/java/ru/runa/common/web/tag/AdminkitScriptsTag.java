package ru.runa.common.web.tag;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import ru.runa.common.web.Commons;
import ru.runa.common.web.action.AdminkitScriptsAction;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.commons.web.PortletUrlType;

import com.google.common.base.Throwables;

public class AdminkitScriptsTag extends TagSupport {
    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag() {
        try {
            String html = "";
            File dirFile = new File(IOCommons.getAdminkitScriptsDirPath());
            if (dirFile.exists() && dirFile.isDirectory()) {
                for (File file : dirFile.listFiles()) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        html += "<a href=\"#\" fileName=\"" + fileName + "\">" + fileName + "</a>&nbsp;";
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("action", "delete");
                        params.put("fileName", fileName);
                        String href = Commons.getActionUrl(AdminkitScriptsAction.PATH, params, pageContext, PortletUrlType.Action);
                        html += "(<a href=\"" + href + "\">X</a>)";
                        html += "&nbsp;&nbsp;";
                    }
                }
            } else {
                html += "not valid directory: " + IOCommons.getAdminkitScriptsDirPath();
            }
            pageContext.getOut().write(html);
            return Tag.SKIP_BODY;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
