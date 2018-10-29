package ru.runa.common.web.tag;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.ActionExceptionHelper;

/**
 * Created on 07.09.2004
 *
 * Tag translate global JSP exceptions declared in web.xml into Struts ActionErrors and save them
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "globalExceptions")
public class GlobalExceptions extends TagSupport {
    private static final long serialVersionUID = 1L;

    private static final String EXCEPTION_REQUEST_ATTRIBUTE_NAME = "javax.servlet.error.exception";

    @Override
    public int doStartTag() {
        Exception exception = (Exception) pageContext.getRequest().getAttribute(EXCEPTION_REQUEST_ATTRIBUTE_NAME);
        if (exception != null) {
            ActionExceptionHelper.addException(getActionErrors(), exception, pageContext.getRequest().getLocale());
        }
        return SKIP_BODY;
    }

    private ActionMessages getActionErrors() {
        ActionMessages messages = (ActionMessages) pageContext.getRequest().getAttribute(Globals.ERROR_KEY);
        if (messages == null) {
            messages = new ActionMessages();
            pageContext.getRequest().setAttribute(Globals.ERROR_KEY, messages);
        }
        return messages;
    }
}
