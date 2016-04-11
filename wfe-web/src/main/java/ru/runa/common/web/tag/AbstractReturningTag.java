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

import org.apache.ecs.html.Form;

/**
 * Created on 01.02.2005
 * 
 */
public abstract class AbstractReturningTag extends TagSupport implements ReturningTag {
    private static final long serialVersionUID = 1L;

    private String returnAction;
    private String action;
    private String method = Form.POST;

    public void setAction(String action) {
        this.action = action;
    }

    public void setMethod(String string) {
        method = string;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getAction() {
        return action;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getMethod() {
        return method;
    }

    /**
     * @jsp.attribute required = "true" rtexprvalue = "true"
     */
    @Override
    public void setReturnAction(String forwardName) {
        returnAction = forwardName;
    }

    @Override
    public String getReturnAction() {
        return returnAction;
    }
}
