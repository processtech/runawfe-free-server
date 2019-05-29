package ru.runa.wfe.office.doc;

import java.util.List;
import org.dom4j.Element;
import ru.runa.wfe.office.doc.MergeDocxConfig.DocxInfo;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;

public class MergeDocxConfigParser extends FilesSupplierConfigParser<MergeDocxConfig> {

    @Override
    protected MergeDocxConfig instantiate() {
        return new MergeDocxConfig();
    }

    @Override
    protected void parseCustom(Element root, MergeDocxConfig config) throws Exception {
        List<Element> inputElements = root.elements("input");
        for (Element inputElement : inputElements) {
            DocxInfo info = new DocxInfo();
            info.addBreak = Boolean.parseBoolean(inputElement.attributeValue("addBreak", "true"));
            info.path = inputElement.attributeValue("path");
            info.variableName = inputElement.attributeValue("variable");
            config.getInputFileInfos().add(info);
        }
    }
}
