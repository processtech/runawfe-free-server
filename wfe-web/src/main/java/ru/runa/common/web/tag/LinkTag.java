/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.common.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.tldgen.annotations.Attribute;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.wfe.commons.web.PortletUrlType;

public abstract class LinkTag extends VisibleTag {

    private static final long serialVersionUID = -6333366313026520201L;

    private String href = "";

    private String linkText = "";

    /**
     * @return true if link must be enabled
     */
    protected boolean isLinkEnabled() {
        return true;
    }

    @Override
    protected ConcreteElement getEndElement() {
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

    protected String getLinkText() {
        return linkText;
    }

    protected String getHref() {
        return href;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setForward(String forward) {
        href = Commons.getForwardUrl(forward, pageContext, PortletUrlType.Action);
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setHref(String href) {
        this.href = Commons.getActionUrl(href, pageContext, PortletUrlType.Render);
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setLinkText(String linkText) {
        this.linkText = linkText;
    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }
}
