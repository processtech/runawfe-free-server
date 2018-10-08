package ru.runa.wfe.office.shared;

import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;

public abstract class FilesSupplierConfigParser<T extends FilesSupplierConfig> {

    protected abstract T instantiate();

    protected abstract void parseCustom(Element root, T config) throws Exception;

    public final T parse(String xml) throws Exception {
        T config = instantiate();
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
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

}
