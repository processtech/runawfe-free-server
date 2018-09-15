package ru.runa.wfe.definition.par;

import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.definition.bpmn.BpmnXmlReader;
import ru.runa.wfe.definition.jpdl.JpdlXmlReader;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;

public class ProcessDefinitionParser implements ProcessArchiveParser {

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return true;
    }

    @Override
    public void readFromArchive(ProcessArchive processArchive, ParsedProcessDefinition parsedProcessDefinition) {
        String fileName = FileDataProvider.PROCESSDEFINITION_XML_FILE_NAME;
        if (parsedProcessDefinition instanceof ParsedSubprocessDefinition) {
            fileName = parsedProcessDefinition.getNodeId() + "." + fileName;
        }
        byte[] definitionXml = parsedProcessDefinition.getFileDataNotNull(fileName);
        Document document = XmlUtils.parseWithoutValidation(definitionXml);
        Element root = document.getRootElement();
        if ("process-definition".equals(root.getName())) {
            JpdlXmlReader reader = ApplicationContextFactory.autowireBean(new JpdlXmlReader(document));
            reader.readProcessDefinition(parsedProcessDefinition);
            parsedProcessDefinition.getProcessDefinition().setLanguage(Language.JPDL);
        } else if ("definitions".equals(root.getName())) {
            BpmnXmlReader reader = ApplicationContextFactory.autowireBean(new BpmnXmlReader(document));
            reader.readProcessDefinition(parsedProcessDefinition);
            parsedProcessDefinition.getProcessDefinition().setLanguage(Language.BPMN2);
        } else {
            throw new InternalApplicationException("Couldn't determine language from content");
        }
    }
}
