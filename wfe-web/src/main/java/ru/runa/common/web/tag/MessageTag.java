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

/**
 * Created on 06.09.2004
 * 
 * @jsp.tag name = "message" body-content = "empty"
 */
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

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
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
