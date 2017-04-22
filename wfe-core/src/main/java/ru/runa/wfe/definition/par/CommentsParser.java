package ru.runa.wfe.definition.par;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.lang.ProcessDefinition;

public class CommentsParser implements ProcessArchiveParser {
    private static final String VERSION = "version";
    private static final String DATE = "date";
    private static final String AUTHOR = "author";
    private static final String COMMENT = "comment";

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return true;
    }

    @Override
    public void readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition) {
        byte[] definitionXml = processDefinition.getFileData(IFileDataProvider.COMMENTS_XML_FILE_NAME);
        if (definitionXml != null) {
            processDefinition.setChanges(parse(definitionXml));
        }
    }

    private List<ProcessDefinitionChange> parse(byte[] definitionXml) {
        List<ProcessDefinitionChange> result = new ArrayList<>();
        Document document = XmlUtils.parseWithoutValidation(definitionXml);
        List<Element> versionList = document.getRootElement().elements(VERSION);
        for (Element element : versionList) {
            Date date = CalendarUtil.convertToDate(element.elementText(DATE), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
            result.add(new ProcessDefinitionChange(date, element.elementText(AUTHOR), element.elementText(COMMENT)));
        }
        return result;
    }
}
