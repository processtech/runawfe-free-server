package ru.runa.wfe.datasource;

import org.dom4j.Document;
import org.dom4j.Element;

public class RedmineDataSource extends DataSource {

    private String projectIdentifier;

    @Override
    void init(Document document) {
        super.init(document);
        Element root = document.getRootElement();
        projectIdentifier = root.elementText(ELEMENT_PROJECT_IDENTIFIER);
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }
}
