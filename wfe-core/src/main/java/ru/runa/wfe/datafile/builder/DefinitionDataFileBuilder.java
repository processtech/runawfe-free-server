package ru.runa.wfe.datafile.builder;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.User;

/**
 * Populate zip archive definition files. Add action 'deployProcessDefinition'
 * to xml.
 * 
 * @author riven
 * 
 */
@Component
public class DefinitionDataFileBuilder implements DataFileBuilder {

    @Autowired
    private DefinitionLogic definitionLogic;

    @Override
    public void build(ZipOutputStream zos, Document script, User user) throws IOException {
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS.createNonPaged();
        List<WfDefinition> definitions = definitionLogic.getProcessDefinitions(user, batchPresentation, false);
        for (WfDefinition definition : definitions) {
            String fileName = definition.getName() + "." + FileDataProvider.PAR_FILE;
            byte[] definitionPar = definitionLogic.getFile(user, definition.getId(), FileDataProvider.PAR_FILE);
            ZipEntry zipEntry = new ZipEntry(PATH_TO_PROCESS_DEF + fileName);
            zos.putNextEntry(zipEntry);
            zos.write(definitionPar, 0, definitionPar.length);
            zos.closeEntry();
            Element element = script.getRootElement().addElement("deployProcessDefinition", XmlUtils.RUNA_NAMESPACE);
            element.addAttribute("file", PATH_TO_PROCESS_DEF + fileName);
            element.addAttribute("type", definition.getCategories()[0]);
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
