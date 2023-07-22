package ru.runa.wfe.datasource;

import org.dom4j.Document;
import org.dom4j.Element;

public class ExcelDataSource extends DataSource {

    private String filePath;

    @Override
    void init(Document document) {
        super.init(document);
        Element root = document.getRootElement();
        filePath = root.elementText(ELEMENT_FILE_PATH);
    }

    public String getFilePath() {
        return filePath;
    }
}
