package ru.runa.wfe.service.utils;

import org.dom4j.Document;
import ru.runa.wfe.commons.xml.XmlUtils;

public class AdminScriptUtils {

    public static Document createScriptDocument() {
        return XmlUtils.createDocument("workflowScript", XmlUtils.RUNA_NAMESPACE);
    }

}
