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
