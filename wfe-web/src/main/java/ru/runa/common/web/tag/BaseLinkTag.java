package ru.runa.common.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import ru.runa.common.web.Resources;

/**
 * LinkTag logic without JSP attributes. Some link tags don't need "href" and "linkText" attributes available in JSP.
 * Those must be inherited from this class instead of LinkTag.
 */
public abstract class BaseLinkTag extends VisibleTag {
    private static final long serialVersionUID = -6333366313026520201L;

    protected boolean isLinkEnabled() {
        return true;
    }

    protected abstract String getLinkText();

    protected abstract String getHref();

    @Override
    protected final ConcreteElement getStartElement() {
        return new StringElement();
    }

    @Override
    protected final ConcreteElement getEndElement() {
        ConcreteElement concreteElement;
        try {
            if (isLinkEnabled()) {
                concreteElement = new A(getHref(), getLinkText());
            } else {
                concreteElement = new StringElement();
            }
        } catch (Exception e) {
            log.debug("link.isEnabled", e);
            concreteElement = new StringElement();
        }
        concreteElement.setClass(Resources.CLASS_LINK);
        return concreteElement;
    }
}
