package ru.runa.common.web.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.tldgen.annotations.BodyContent;

import com.google.common.base.Throwables;

import ru.runa.common.web.Commons;
import ru.runa.common.web.action.AdminkitScriptsAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "viewAdminkitScripts")
public class AdminkitScriptsTag extends TagSupport {
    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag() {
        try {
            final ScriptingService scriptingService = Delegates.getScriptingService();
            final StringBuilder html = new StringBuilder();
            for (String scriptName : scriptingService.getScriptsNames()) {
                html.append("<a href=\"#\" fileName=\"").append(scriptName).append("\">").append(scriptName).append("</a>&nbsp;");
                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "delete");
                params.put("fileName", scriptName);
                String href = Commons.getActionUrl(AdminkitScriptsAction.PATH, params, pageContext, PortletUrlType.Action);
                html.append("(<a href=\"").append(href).append("\">X</a>)").append("&nbsp;&nbsp;");
            }
            pageContext.getOut().write(html.toString());
            return Tag.SKIP_BODY;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

}
