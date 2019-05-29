package ru.runa.wf.logic.bot.cancelprocess;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.collect.Maps;

public class CancelProcessTaskXmlParser {
    private static final String PROCESS_TO_CANCEL = "processToCancel";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String TASK_HANDLER_CONFIGURATION_ATTRIBUT_NAME = "taskHandlerConfiguration";
    private static final String PROCESS_ID_VARIABLE_ATTRIBUTE_NAME = "processIdVariable";

    /**
     * Parses DatabaseTaskHandler configuration.
     */
    public static CancelProcessTask parse(String configuration) throws Exception {
        Document document = XmlUtils.parseWithXSDValidation(configuration, "cancel-process.xsd");
        Element processesElement = document.getRootElement();
        String processIdVariable = processesElement.attributeValue(PROCESS_ID_VARIABLE_ATTRIBUTE_NAME);
        List<Element> processToCancelElements = processesElement.elements(PROCESS_TO_CANCEL);
        Map<String, String> taskMap = Maps.newHashMap();
        for (Element processEllement : processToCancelElements) {
            String name = processEllement.attributeValue(NAME_ATTRIBUTE_NAME);
            String handlerConfiguration = processEllement.attributeValue(TASK_HANDLER_CONFIGURATION_ATTRIBUT_NAME);
            taskMap.put(name, handlerConfiguration);
        }
        return new CancelProcessTask(processIdVariable, taskMap);
    }
}
