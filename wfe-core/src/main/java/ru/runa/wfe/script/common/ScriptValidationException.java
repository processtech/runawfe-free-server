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
package ru.runa.wfe.script.common;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import ru.runa.wfe.InternalApplicationException;

public class ScriptValidationException extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    public ScriptValidationException(String msg) {
        super(msg);
    }

    public ScriptValidationException(ScriptOperation operation, String errorDescription) {
        super(errorDescription + "\n" + getMessage(operation));
    }

    public ScriptValidationException(ScriptOperation operation, Throwable cause) {
        super(getMessage(operation), cause);
    }

    private static String getMessage(ScriptOperation operation) {
        if (operation == null) {
            return "Configuration error detected";
        }
        try {
            StringWriter writer = new StringWriter();
            JAXBContext.newInstance(operation.getClass()).createMarshaller().marshal(operation, writer);
            return "Configuration error detected in " + writer.toString();
        } catch (JAXBException e) {
            return "Configuration error detected";
        }
    }
}
