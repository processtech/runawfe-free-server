package ru.runa.wf.web.datafile.builder;

import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Populate zip archive definition files. Add action 'deployProcessDefinition'
 * to xml.
 * 
 * @author riven
 * 
 */
public class DefinitionDataFileBuilder implements DataFileBuilder {
    protected final Log log = LogFactory.getLog(getClass());
    private final User user;

    public DefinitionDataFileBuilder(User user) {
        this.user = user;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) throws Exception {
        DefinitionService definitionService = Delegates.getDefinitionService();
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS.createNonPaged();
        List<WfDefinition> definitions = definitionService.getProcessDefinitions(user, batchPresentation, false);
        for (WfDefinition definition : definitions) {
            String fileName = definition.getName() + "." + FileDataProvider.PAR_FILE;
            byte[] definitionPar = definitionService.getProcessDefinitionFile(user, definition.getId(), FileDataProvider.PAR_FILE);
            ZipEntry zipEntry = new ZipEntry(PATH_TO_PROCESS_DEF + fileName);
            zos.putNextEntry(zipEntry);
            zos.write(definitionPar, 0, definitionPar.length);
            zos.closeEntry();
            Element element = script.getRootElement().addElement("deployProcessDefinition", XmlUtils.RUNA_NAMESPACE);
            element.addAttribute("file", PATH_TO_PROCESS_DEF + fileName);
            element.addAttribute("type", definition.getCategories()[0]);
        }
    }
}
