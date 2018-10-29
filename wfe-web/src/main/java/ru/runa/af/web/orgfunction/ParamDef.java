package ru.runa.af.web.orgfunction;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Messages;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;

public class ParamDef {
    private final String messageKey;
    private final String message;
    private final ParamRenderer renderer;

    public ParamDef(String messageKey, String message, ParamRenderer renderer) {
        this.messageKey = messageKey;
        this.message = message;
        this.renderer = renderer;
    }

    public String getMessage(PageContext pageContext) {
        if (message != null) {
            return message;
        }
        return Messages.getMessage(messageKey, pageContext);
    }

    public ParamRenderer getRenderer() {
        return renderer;
    }

}
