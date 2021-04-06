package ru.runa.common.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "message")
public class MessageTag extends VisibleTag {

    private static final long serialVersionUID = -1765787772164997739L;
    String message = "";

    @Override
    protected ConcreteElement getEndElement() {
        StringElement stringElement = new StringElement(getMessage());
        return stringElement;
    }

    public String getMessage() {
        return message;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setMessage(String message) {
        if (message == null) {
            message = "";
        }
        this.message = message;

    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }
}
