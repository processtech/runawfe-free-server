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
package ru.runa.af.web.tag;

import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.html.A;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.LogoutAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * Provides logout Created on 19.08.2004
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "logout")
public class LogoutTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    @Override
    public int doStartTag() {
        A logoutHref = new A();
        String actionURL = Commons.getActionUrl(LogoutAction.ACTION_NAME, pageContext, PortletUrlType.Action);
        logoutHref.setHref(actionURL);
        String logoutText = MessagesCommon.LOGOUT.message(pageContext);
        logoutHref.setTagText(logoutText);
        logoutHref.output(pageContext.getOut());
        return Tag.SKIP_BODY;
    }
}
