package ru.runa.wfe.datasource;

import org.dom4j.Document;
import org.dom4j.Element;

public class JdbcDataSource extends DataSource {

    private JdbcDataSourceType dbType;
    private String url;
    private String dbName;
    private String userName;
    private String password;

    @Override
    void init(Document document) {
        super.init(document);
        Element root = document.getRootElement();
        dbType = JdbcDataSourceType.valueOf(root.elementText(ELEMENT_DB_TYPE));
        url = root.elementText(ELEMENT_DB_URL);
        dbName = root.elementText(ELEMENT_DB_NAME);
        userName = root.elementText(ELEMENT_USER_NAME);
        password = root.elementText(ELEMENT_PASSWORD);
    }

    public JdbcDataSourceType getDbType() {
        return dbType;
    }

    public String getUrl() {
        return url;
    }

    public String getDbName() {
        return dbName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

}
