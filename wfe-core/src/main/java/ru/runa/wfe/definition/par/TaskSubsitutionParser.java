package ru.runa.wfe.definition.par;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.ParsedProcessDefinition;

/**
 * used only for processes created before v4.1.0
 */
@CommonsLog
public class TaskSubsitutionParser implements ProcessArchiveParser {

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return false;
    }

    @Override
    public void readFromArchive(ProcessArchive archive, final ParsedProcessDefinition parsedProcessDefinition) {
        byte[] xml = parsedProcessDefinition.getFileData(IFileDataProvider.SUBSTITUTION_EXCEPTIONS_FILE_NAME);
        if (xml == null) {
            return;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        Element root = document.getRootElement();
        List<Element> elements = root.elements("task");
        for (Element element : elements) {
            String nodeId = element.attributeValue("name");
            InteractionNode interactionNode = (InteractionNode) parsedProcessDefinition.getNode(nodeId);
            if (interactionNode == null) {
                log.warn("No node found by id '" + nodeId + "' in " + parsedProcessDefinition);
                continue;
            }
            interactionNode.getFirstTaskNotNull().setIgnoreSubsitutionRules(true);
        }
    }

}
