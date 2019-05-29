package ru.runa.wf.web.tag;

import javax.servlet.http.HttpServletRequest;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.tag.BaseLinkTag;

/**
 * If "return" request parameter is present, then link is visible, enabled and its "href" attribute is equal to "action" parameter value.
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "returnLinkTag")
public class ReturnLinkTag extends BaseLinkTag {
    private static final long serialVersionUID = 1L;

    private String getReturnAction() {
        HttpServletRequest rq = (HttpServletRequest)pageContext.getRequest();
        String s = rq.getParameter("return");
        return s == null ? null : rq.getContextPath() + s;
    }

    @Override
    protected boolean isVisible() {
        return getReturnAction() != null;
    }

    @Override
    protected String getHref() {
        return getReturnAction();
    }

    @Override
    protected String getLinkText() {
        return MessagesOther.TITLE_BACK.message(pageContext);
    }
}
