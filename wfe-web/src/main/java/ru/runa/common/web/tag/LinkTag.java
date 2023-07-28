package ru.runa.common.web.tag;

import org.tldgen.annotations.Attribute;
import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.web.PortletUrlType;

public abstract class LinkTag extends BaseLinkTag {
    private static final long serialVersionUID = -6333366313026520201L;

    private String href = "";
    private String linkText = "";

    @Override
    protected String getLinkText() {
        return linkText;
    }

    @Override
    protected String getHref() {
        return href;
    }

    @Attribute
    public void setForward(String forward) {
        href = Commons.getForwardUrl(forward, pageContext, PortletUrlType.Action);
    }

    @Attribute
    public void setHref(String href) {
        this.href = Commons.getActionUrl(href, pageContext, PortletUrlType.Render);
    }

    @Attribute
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }
}
