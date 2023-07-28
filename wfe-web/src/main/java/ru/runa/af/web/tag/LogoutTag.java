package ru.runa.af.web.tag;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.html.A;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.LogoutAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * Provides logout Created on 19.08.2004
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "logout")
public class LogoutTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag() {
        A logoutHref = new A();
        String actionURL = Commons.getActionUrl(LogoutAction.ACTION_NAME, pageContext, PortletUrlType.Action);
        logoutHref.setHref(actionURL);
        String logoutText = MessagesCommon.LOGOUT.message(pageContext);
        logoutHref.setTagText(logoutText);
        logoutHref.output(pageContext.getOut());
        return Tag.SKIP_BODY;
    }
}
