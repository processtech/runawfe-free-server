package ru.runa.wf.logic.bot.assigner;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Preconditions;

public class AssignerSettingsXmlParser {
    private static final String CONDITIONS_ELEMENT_NAME = "conditions";
    private static final String CONDITION_ELEMENT_NAME = "condition";
    private static final String SWIMLANE_ELEMENT_NAME = "swimlaneName";
    private static final String FUNCTION_ELEMENT_NAME = "function";
    private static final String VARIABLE_ELEMENT_NAME = "variableName";

    public static AssignerSettings read(String configuration) throws Exception {
        AssignerSettings assignerSettings = new AssignerSettings();
        Document document = XmlUtils.parseWithXSDValidation(configuration, "assigner.xsd");
        Element conditionsElement = document.getRootElement().element(CONDITIONS_ELEMENT_NAME);
        List<Element> elements = conditionsElement.elements(CONDITION_ELEMENT_NAME);
        for (Element conditionElement : elements) {
            String swimlaneName = conditionElement.elementTextTrim(SWIMLANE_ELEMENT_NAME);
            String functionClassName = conditionElement.elementTextTrim(FUNCTION_ELEMENT_NAME);
            String variableName = conditionElement.elementTextTrim(VARIABLE_ELEMENT_NAME);
            Preconditions.checkNotNull(swimlaneName, SWIMLANE_ELEMENT_NAME);
            Preconditions.checkNotNull(functionClassName, FUNCTION_ELEMENT_NAME);
            Preconditions.checkNotNull(variableName, VARIABLE_ELEMENT_NAME);
            assignerSettings.addAssignerCondition(new AssignerSettings.Condition(swimlaneName, functionClassName, variableName));
        }
        return assignerSettings;
    }
}
