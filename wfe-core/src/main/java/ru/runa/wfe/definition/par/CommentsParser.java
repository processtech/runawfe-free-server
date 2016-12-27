package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.definition.VersionInfo;

public class CommentsParser implements ProcessArchiveParser {
    public static final String VERSION = "version";
    public static final String VERSION_DATE = "date";
    public static final String VERSION_AUTHOR = "author";
    public static final String VERSION_COMMENT = "comment";

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return true;
    }

    @Override
    public void readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition) {
        String fileName = IFileDataProvider.COMMENTS_XML_FILE_NAME;

        if (processArchive.getFileData().containsKey(fileName)) {
            byte[] definitionXml = processDefinition.getFileDataNotNull(fileName);
            Document document = XmlUtils.parseWithoutValidation(definitionXml);
            List<Element> versionList = document.getRootElement().elements(VERSION);
            for (Element versionInfoElement : versionList) {
                VersionInfo versionInfo = new VersionInfo();
                versionInfo.setDateTime(versionInfoElement.elementText(VERSION_DATE));
                versionInfo.setAuthor(versionInfoElement.elementText(VERSION_AUTHOR));
                versionInfo.setComment(versionInfoElement.elementText(VERSION_COMMENT));

                processDefinition.addToVersionInfoList(versionInfo);
            }
        }
    }
}
