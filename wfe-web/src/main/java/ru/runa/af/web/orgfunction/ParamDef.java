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
