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
package ru.runa.wfe.commons.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Created on 25.07.2005
 * 
 */
public class SimpleErrorHandler implements ErrorHandler {
    private static final SimpleErrorHandler INSTANCE = new SimpleErrorHandler();

    private SimpleErrorHandler() {
        // privents singleton instance creation
    }

    public static ErrorHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void error(SAXParseException exception) throws SAXParseException {
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXParseException {
        throw exception;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXParseException {
        throw exception;
    }
}
