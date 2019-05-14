package ru.runa.wfe.datasource;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.util.StringJoiner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.google.common.base.Strings;

public class JdbcDataSource extends DataSource {

    private static final Log log = LogFactory.getLog(JdbcDataSource.class);

    private JdbcDataSourceType dbType;
    private String url;
    private String dbName;
    private String userName;
    private String password;

    @Override
    void init(Document document) {
        super.init(document);
        Element root = document.getRootElement();
        String dbTypeStored = root.elementText(ELEMENT_DB_TYPE);
        dbType = Strings.isNullOrEmpty(dbTypeStored) ? null : JdbcDataSourceType.valueOf(dbTypeStored);
        url = Strings.emptyToNull(root.elementText(ELEMENT_DB_URL));
        dbName = Strings.emptyToNull(root.elementText(ELEMENT_DB_NAME));
        userName = Strings.emptyToNull(root.elementText(ELEMENT_USER_NAME));
        password = Strings.emptyToNull(root.elementText(ELEMENT_PASSWORD));
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

    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(getDbType() == null ? getUrl() : DataSourceStuff.adjustUrl(this), getUserName(), getPassword());
    }

    /**
     * Returns the version information of the current database server. Throws Exception if occurred.
     * 
     * @return Information about the current database server (String)
     */
    public String serverVersion() throws Exception {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metadata = conn.getMetaData();
            StringJoiner sj = new StringJoiner("; ");
            sj.add("Product name: " + metadata.getDatabaseProductName());
            sj.add("Product version: " + metadata.getDatabaseProductVersion());
            sj.add("Driver name: " + metadata.getDriverName());
            sj.add("Driver version: " + metadata.getDriverVersion());
            return sj.toString();
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

}
