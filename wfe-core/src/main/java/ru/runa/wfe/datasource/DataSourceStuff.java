package ru.runa.wfe.datasource;

import com.google.common.base.Strings;

public interface DataSourceStuff {

    String DATA_SOURCE_FILE_SUFFIX = ".xml";
    String DATA_SOURCE_ARCHIVE_SUFFIX = ".ds";

    String DATABASE_NAME_MARKER = "DBNAME";

    String ELEMENT_DATA_SOURCE = "dataSource";
    String ELEMENT_FILE_PATH = "filePath";
    String ELEMENT_FILE_NAME = "fileName";
    String ELEMENT_JNDI_NAME = "jndiName";
    String ELEMENT_DB_TYPE = "dbType";
    String ELEMENT_DB_URL = "dbUrl";
    String ELEMENT_DB_NAME = "dbName";
    String ELEMENT_USER_NAME = "userName";
    String ELEMENT_PASSWORD = "password";

    String ATTR_NAME = "name";
    String ATTR_TYPE = "type";

    String PATH_PREFIX_JNDI_NAME = "jndiname:";
    String PATH_PREFIX_JNDI_NAME_VARIABLE = "jndiname-variable:";
    String PATH_PREFIX_DATA_SOURCE = "datasource:";
    String PATH_PREFIX_DATA_SOURCE_VARIABLE = "datasource-variable:";

    String JNDI_NAME_SAMPLE = "jboss/datasources/<DS>";

    static String adjustUrl(JdbcDataSource jds) {
        String url = jds.getUrl();
        String dbName = Strings.isNullOrEmpty(jds.getDbName()) ? "__DB_UNDEFINED__" : jds.getDbName();
        if (url.contains(DATABASE_NAME_MARKER)) {
            url = url.replace(DATABASE_NAME_MARKER, dbName);
        } else {
            url = url + jds.getDbType().getDatabaseDelimiter() + dbName;
        }
        return url;
    }

}
