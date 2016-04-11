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

import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.ActionExceptionHelper;

/**
 * Created on 07.09.2004
 * 
 * @jsp.tag name = "globalExceptions" body-content = "empty" description =
 *          "Tag translate global JSP exceptions declared in web.xml into Struts ActionErrors and save them"
 */
public class GlobalExceptions extends TagSupport {
    private static final long serialVersionUID = 1L;

    private static final String EXCEPTION_REQUEST_ATTRIBUTE_NAME = "javax.servlet.error.exception";

    @Override
    public int doStartTag() {
        Exception exception = (Exception) pageContext.getRequest().getAttribute(EXCEPTION_REQUEST_ATTRIBUTE_NAME);
        if (exception != null) {
            ActionExceptionHelper.addException(getActionErrors(), exception);
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
