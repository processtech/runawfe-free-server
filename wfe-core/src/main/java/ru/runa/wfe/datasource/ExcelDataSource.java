package ru.runa.wfe.datasource;

import org.dom4j.Document;
import org.dom4j.Element;

public class ExcelDataSource extends DataSource {

    private String filePath;
    private String fileName;

    @Override
    void init(Document document) {
        super.init(document);
        Element root = document.getRootElement();
        filePath = root.elementText(ELEMENT_FILE_PATH);
        fileName = root.elementText(ELEMENT_FILE_NAME);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

}
