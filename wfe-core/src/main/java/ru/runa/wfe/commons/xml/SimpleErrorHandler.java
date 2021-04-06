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
