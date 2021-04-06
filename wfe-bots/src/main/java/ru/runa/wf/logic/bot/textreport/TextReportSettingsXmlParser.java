package ru.runa.wf.logic.bot.textreport;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

public class TextReportSettingsXmlParser {
    private static final String TEMPLATE_ELEMENT_NAME = "template";
    private static final String FILE_NAME_ATTRIBUTE_NAME = "fileName";
    private static final String FILE_ENCODING_ATTRIBUTE_NAME = "fileEncoding";
    private static final String VARIABLE_NAME_ATTRIBUTE_NAME = "variableName";
    private static final String CONTENT_TYPE_ATTRIBUTE_NAME = "contentType";
    private static final String REPORT_ELEMENT_NAME = "report";
    private static final String REPLACEMENTS_ELEMENT_NAME = "replacements";
    private static final String REPLACEMENT_ELEMENT_NAME = "replacement";
    private static final String SOURCE_ATTRIBUTE_NAME = "source";
    private static final String DESTINATION_ATTRIBUTE_NAME = "dest";
    private static final String XML_FORMAT_ATTRIBUTE_NAME = "xmlFormat";
    private static final String APPLY_TO_REGEXP_ATTRIBUTE_NAME = "applyToRegexp";

    public static TextReportSettings read(String configuration) {
        TextReportSettings textReportSettings = new TextReportSettings();
        Document document = XmlUtils.parseWithXSDValidation(configuration, "textreport.xsd");

        Element root = document.getRootElement();
        Element templateElement = root.element(TEMPLATE_ELEMENT_NAME);
        textReportSettings.setTemplateFileName(templateElement.attributeValue(FILE_NAME_ATTRIBUTE_NAME));
        textReportSettings.setTemplateEncoding(templateElement.attributeValue(FILE_ENCODING_ATTRIBUTE_NAME));

        Element reportElement = root.element(REPORT_ELEMENT_NAME);
        textReportSettings.setReportFileName(reportElement.attributeValue(FILE_NAME_ATTRIBUTE_NAME));
        textReportSettings.setReportEncoding(reportElement.attributeValue(FILE_ENCODING_ATTRIBUTE_NAME));
        textReportSettings.setReportContentType(reportElement.attributeValue(CONTENT_TYPE_ATTRIBUTE_NAME));
        textReportSettings.setReportVariableName(reportElement.attributeValue(VARIABLE_NAME_ATTRIBUTE_NAME));

        Element replacementElement = root.element(REPLACEMENTS_ELEMENT_NAME);
        if (replacementElement != null) {
            textReportSettings.setXmlFormatSupport("true".equals(replacementElement.attributeValue(XML_FORMAT_ATTRIBUTE_NAME)));
            textReportSettings.setApplyToRegexp("true".equals(replacementElement.attributeValue(APPLY_TO_REGEXP_ATTRIBUTE_NAME)));
            List<Element> nodeList = replacementElement.elements(REPLACEMENT_ELEMENT_NAME);
            String[] replacementSources = new String[nodeList.size()];
            String[] replacementDestinations = new String[nodeList.size()];
            for (int i = 0; i < nodeList.size(); i++) {
                Element replacementNode = nodeList.get(i);
                replacementSources[i] = replacementNode.attributeValue(SOURCE_ATTRIBUTE_NAME);
                replacementDestinations[i] = replacementNode.attributeValue(DESTINATION_ATTRIBUTE_NAME);
            }
            textReportSettings.setReplacements(replacementSources, replacementDestinations);
        }
        return textReportSettings;
    }
}
