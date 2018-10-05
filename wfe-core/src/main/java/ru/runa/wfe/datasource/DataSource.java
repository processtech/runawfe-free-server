package ru.runa.wfe.datasource;

import org.dom4j.Document;
import org.dom4j.Element;

public abstract class DataSource implements DataSourceStuff {

    protected String name;
    protected DataSourceType type;

    void init(Document doc) {
        Element root = doc.getRootElement();
        name = root.attributeValue(ATTR_NAME);
        type = DataSourceType.valueOf(root.attributeValue(ATTR_TYPE));
    }

    public String getName() {
        return name;
    }

    public DataSourceType getType() {
        return type;
    }

}
