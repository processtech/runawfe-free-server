package ru.runa.wf.logic.bot.mswordreport;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

public class MsWordReportSettingsXmlParser {
    private static final String REPORT_ELEMENT_NAME = "report";
    private static final String TEMPLATE_FILE_PATH_ATTRIBUTE_NAME = "template-path";
    private static final String OUTPUT_VARIABLE_FILE_ATTRIBUTE_NAME = "output-variable-file-name";
    private static final String OUTPUT_VARIABLE_ATTRIBUTE_NAME = "output-variable";
    private static final String STRICT_MODE_ATTRIBUTE_NAME = "strict-mode";
    private static final String MAPPING_ELEMENT_NAME = "mapping";
    private static final String BOOKMARK_ATTRIBUTE_NAME = "bookmark";
    private static final String VARIABLE_ATTRIBUTE_NAME = "variable";

    public static MsWordReportTaskSettings read(String configuration) {
        Document document = XmlUtils.parseWithXSDValidation(configuration, "msword-report-task.xsd");
        Element reportElement = document.getRootElement().element(REPORT_ELEMENT_NAME);
        String templatePath = reportElement.attributeValue(TEMPLATE_FILE_PATH_ATTRIBUTE_NAME);
        String fileName = reportElement.attributeValue(OUTPUT_VARIABLE_FILE_ATTRIBUTE_NAME);
        String variableName = reportElement.attributeValue(OUTPUT_VARIABLE_ATTRIBUTE_NAME);
        boolean strictMode = Boolean.parseBoolean(reportElement.attributeValue(STRICT_MODE_ATTRIBUTE_NAME, "false"));
        MsWordReportTaskSettings wordReportSettings = new MsWordReportTaskSettings(strictMode, templatePath, fileName, variableName);
        List<Element> mappingElements = reportElement.elements(MAPPING_ELEMENT_NAME);
        for (Element mappingElement : mappingElements) {
            String bookmark = mappingElement.attributeValue(BOOKMARK_ATTRIBUTE_NAME);
            String variable = mappingElement.attributeValue(VARIABLE_ATTRIBUTE_NAME);
            wordReportSettings.getMappings().add(new BookmarkVariableMapping(bookmark, variable));
        }
        return wordReportSettings;
    }

}
