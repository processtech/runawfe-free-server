package ru.runa.wfe.office.shared;

import java.util.List;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;

public abstract class FilesSupplierConfigParser<T extends FilesSupplierConfig> {

    protected abstract T instantiate();

    protected abstract void parseCustom(Element root, T config) throws Exception;

    public final T parse(String xml) throws Exception {
        T config = instantiate();
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        if (!SystemProperties.isFileSystemAccessAllowed() && isFileSystemAccessNeeded(root)) {
            throw new Exception("Access to server file system is not allowed");
        }
        Element inputElement = root.element("input");
        if (inputElement != null) {
            config.setInputFilePath(inputElement.attributeValue("path"));
            config.setInputFileVariableName(inputElement.attributeValue("variable"));
        }
        Element outputElement = root.element("output");
        if (outputElement != null) {
            config.setOutputDirPath(outputElement.attributeValue("dir"));
            config.setOutputFileVariableName(outputElement.attributeValue("variable"));
            config.setOutputFileName(outputElement.attributeValue("fileName"));
        }
        parseCustom(root, config);
        return config;
    }

    private boolean isFileSystemAccessNeeded(Element root) {
        return isFileSystemAccessNeeded(root.elements("input")) || isFileSystemAccessNeeded(root.elements("output"));
    }

    private boolean isFileSystemAccessNeeded(List<?> elements) {
        for (Object elementObject : elements) {
            if (elementObject instanceof Element) {
                Element element = (Element) elementObject;
                List<?> attributes = element.attributes();
                for (Object attributeObject : attributes) {
                    if (attributeObject instanceof Attribute) {
                        Attribute attribute = (Attribute) attributeObject;
                        String path = attribute.getValue();
                        String name = attribute.getName();
                        if ((name.equals("path") || name.equals("dir")) && isFileSystemAccessPath(path)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isFileSystemAccessPath(String path) {
        return path != null &&
                !path.startsWith(FileDataProvider.PROCESS_FILE_PROTOCOL) &&
                !path.startsWith(FileDataProvider.BOT_TASK_FILE_PROTOCOL) &&
                !path.startsWith("datasource:") &&
                !path.startsWith("datasource-variable:");
    }

}
