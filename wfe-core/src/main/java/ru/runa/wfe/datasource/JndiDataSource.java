package ru.runa.wfe.datasource;

import org.dom4j.Document;

public class JndiDataSource extends DataSource {

    private String jndiName;

    @Override
    void init(Document document) {
        super.init(document);
        jndiName = document.getRootElement().elementText(ELEMENT_JNDI_NAME);
    }

    public String getJndiName() {
        return jndiName;
    }

}
