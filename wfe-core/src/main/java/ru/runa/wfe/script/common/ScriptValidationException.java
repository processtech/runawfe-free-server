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
