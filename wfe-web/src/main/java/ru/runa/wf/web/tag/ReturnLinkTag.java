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
