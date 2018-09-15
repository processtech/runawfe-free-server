package ru.runa.wfe.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

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

    public Connection getConnection() throws Exception {
        String url = getUrl();
        if (url.contains(DataSourceStuff.DATABASE_NAME_MARKER)) {
            url = url.replace(DataSourceStuff.DATABASE_NAME_MARKER, getDbName());
        } else {
            url = url + (getDbType() == JdbcDataSourceType.Oracle ? ':' : '/') + getDbName();
        }
        Class.forName(getDbType().driverClassName()).newInstance();
        return DriverManager.getConnection(url, getUserName(), getPassword());
    }

    public String serverVersion() {
        //
        // PostgreSql: select version() -> PostgreSQL 9.6.5, compiled by Visual C++ build 1800, 64-bit
        // SQL Server: select @@version -> Microsoft SQL Server 2014 - 12.0.2000.8 (Intel X86)\nFeb 20 2014 19:20:46\nCopyright (c) Microsoft Corporation\nExpress Edition on Windows NT 6.1 <X64> (Build 7601: ) (WOW64)
        // Oracle:     select banner from v$version (WHERE banner LIKE 'Oracle%') -> 5 rows
        // BANNER:
        // Oracle Database 11g Enterprise Edition Release 11.2.0.2.0 - Production
        // PL/SQL Release 11.2.0.2.0 - Production
        // "CORE    11.2.0.2.0  Production"
        // TNS for Linux: Version 11.2.0.2.0 - Production
        // NLSRTL Version 11.2.0.2.0 - Production
        //
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(getDbType().serverVersionQuery());
                ResultSet rs = ps.executeQuery()) {
            rs.next();
            return (String) rs.getObject(1);
        } catch (Exception e) {
            log.error(e);
            return "Server not available";
        }
    }

}
