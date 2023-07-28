package ru.runa.wfe.script;

import org.dom4j.Element;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XmlUtils;

public class AdminScriptException extends InternalApplicationException {

    private static final long serialVersionUID = 1L;

    public AdminScriptException(String msg) {
        super(msg);
    }

    public AdminScriptException(Throwable cause) {
        super(cause);
    }

    public AdminScriptException(Element element, Throwable cause) {
        super("Failed to handle element " + XmlUtils.toString(element), cause);
    }
}
