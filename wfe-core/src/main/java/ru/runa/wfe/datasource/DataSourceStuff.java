package ru.runa.wfe.datasource;

public interface DataSourceStuff {

    public final static String DATA_SOURCE_FILE_SUFFIX = ".xml";
    public final static String DATA_SOURCE_ARCHIVE_SUFFIX = ".ds";

    public final static String DATABASE_NAME_MARKER = "DBNAME";

    public final static String ELEMENT_DATA_SOURCE = "dataSource";
    public final static String ELEMENT_FILE_PATH = "filePath";
    public final static String ELEMENT_FILE_NAME = "fileName";
    public final static String ELEMENT_JNDI_NAME = "jndiName";
    public final static String ELEMENT_DB_TYPE = "dbType";
    public final static String ELEMENT_DB_URL = "dbUrl";
    public final static String ELEMENT_DB_NAME = "dbName";
    public final static String ELEMENT_USER_NAME = "userName";
    public final static String ELEMENT_PASSWORD = "password";

    public final static String ATTR_NAME = "name";
    public final static String ATTR_TYPE = "type";

    public final static String PATH_PREFIX_JNDI_NAME = "jndiname:";
    public final static String PATH_PREFIX_JNDI_NAME_VARIABLE = "jndiname-variable:";
    public final static String PATH_PREFIX_DATA_SOURCE = "datasource:";
    public final static String PATH_PREFIX_DATA_SOURCE_VARIABLE = "datasource-variable:";

    public final static String JNDI_NAME_SAMPLE = "jboss/datasources/<DS>";

}
