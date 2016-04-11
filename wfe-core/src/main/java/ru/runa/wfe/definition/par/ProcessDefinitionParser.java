package ru.runa.wfe.definition.par;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.Language;
import ru.runa.wfe.definition.bpmn.BpmnXmlReader;
import ru.runa.wfe.definition.jpdl.JpdlXmlReader;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

public class ProcessDefinitionParser implements ProcessArchiveParser {

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return true;
    }

    @Override
    public void readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition) {
        String fileName = IFileDataProvider.PROCESSDEFINITION_XML_FILE_NAME;
        if (processDefinition instanceof SubprocessDefinition) {
            fileName = processDefinition.getNodeId() + "." + fileName;
        }
        byte[] definitionXml = processDefinition.getFileDataNotNull(fileName);
        Document document = XmlUtils.parseWithoutValidation(definitionXml);
        Element root = document.getRootElement();
        if ("process-definition".equals(root.getName())) {
            JpdlXmlReader reader = ApplicationContextFactory.autowireBean(new JpdlXmlReader(document));
            reader.readProcessDefinition(processDefinition);
            processDefinition.getDeployment().setLanguage(Language.JPDL);
        } else if ("definitions".equals(root.getName())) {
            BpmnXmlReader reader = ApplicationContextFactory.autowireBean(new BpmnXmlReader(document));
            reader.readProcessDefinition(processDefinition);
            processDefinition.getDeployment().setLanguage(Language.BPMN2);
        } else {
            throw new InternalApplicationException("Couldn't determine language from content");
        }
    }
}
