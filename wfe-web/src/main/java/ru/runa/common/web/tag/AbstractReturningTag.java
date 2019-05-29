package ru.runa.common.web.tag;

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.html.Form;
import org.tldgen.annotations.Attribute;

public abstract class AbstractReturningTag extends TagSupport implements ReturningTag {
    private static final long serialVersionUID = 1L;

    private String returnAction;
    private String action;
    private String method = Form.POST;

    @Attribute(required = false, rtexprvalue = true)
    public void setAction(String action) {
        this.action = action;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setMethod(String string) {
        method = string;
    }

    public String getAction() {
        return action;
    }

    public String getMethod() {
        return method;
    }

    @Attribute(required = true, rtexprvalue = true)
    @Override
    public void setReturnAction(String forwardName) {
        returnAction = forwardName;
    }

    @Override
    public String getReturnAction() {
        return returnAction;
    }
}
