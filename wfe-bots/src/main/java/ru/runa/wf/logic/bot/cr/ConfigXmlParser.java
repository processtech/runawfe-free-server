package ru.runa.wf.logic.bot.cr;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

public class ConfigXmlParser {
    private static final String JNDI_NAME_ATTRIBUTE_NAME = "name";
    private static final String USERNAME_ATTRIBUTE_NAME = "username";
    private static final String PASSWORD_ATTRIBUTE_NAME = "password";
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String OPERATION_ATTRIBUTE_NAME = "operation";
    private static final String VARIABLE_ATTRIBUTE_NAME = "variableName";
    private static final String PATH_ATTRIBUTE_NAME = "path";
    private static final String FILE_ATTRIBUTE_NAME = "fileName";

    public static JcrTaskConfig parse(String configuration) throws Exception {
        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element root = document.getRootElement();
        String repositoryName = root.attributeValue(JNDI_NAME_ATTRIBUTE_NAME);
        String userName = root.attributeValue(USERNAME_ATTRIBUTE_NAME);
        String password = root.attributeValue(PASSWORD_ATTRIBUTE_NAME);
        JcrTaskConfig config = new JcrTaskConfig(repositoryName, userName, password);
        List<Element> taskElements = root.elements(TASK_ELEMENT_NAME);
        for (Element taskElement : taskElements) {
            String operationName = taskElement.attributeValue(OPERATION_ATTRIBUTE_NAME);
            String variableName = taskElement.attributeValue(VARIABLE_ATTRIBUTE_NAME);
            String path = taskElement.attributeValue(PATH_ATTRIBUTE_NAME);
            String fileName = taskElement.attributeValue(FILE_ATTRIBUTE_NAME);
            config.addTask(new JcrTask(operationName, variableName, path, fileName));
        }
        return config;
    }

}
