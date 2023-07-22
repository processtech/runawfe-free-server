package ru.runa.wfe.definition.par;

import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.ProcessDefinition;

/**
 * used only for processes created before v4.1.0
 */
public class TaskSubsitutionParser implements ProcessArchiveParser {

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return false;
    }

    @Override
    public void readFromArchive(ProcessArchive archive, final ProcessDefinition processDefinition) {
        byte[] xml = processDefinition.getFileData(FileDataProvider.SUBSTITUTION_EXCEPTIONS_FILE_NAME);
        if (xml == null) {
            return;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        List<Element> elements = root.elements("task");
        for (Element element : elements) {
            String nodeId = element.attributeValue("name");
            InteractionNode interactionNode = (InteractionNode) processDefinition.getNode(nodeId);
            if (interactionNode == null) {
                LogFactory.getLog(getClass()).warn("No node found by id '" + nodeId + "' in " + processDefinition);
                continue;
            }
            interactionNode.getFirstTaskNotNull().setIgnoreSubsitutionRules(true);
        }
    }

}
