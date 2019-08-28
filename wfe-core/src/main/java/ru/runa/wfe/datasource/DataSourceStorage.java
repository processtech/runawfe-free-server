package ru.runa.wfe.datasource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.springframework.util.FileCopyUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.IoCommons;
import ru.runa.wfe.commons.xml.XmlUtils;

public class DataSourceStorage implements DataSourceStuff {

    private static final Log log = LogFactory.getLog(DataSourceStorage.class);

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(".yyyyMMddHHmmss");

    private static File storageDir;
    private static File storageHistoryDir;

    private static Set<String> driverJarNames = Sets.newHashSet();
    private static Set<JdbcDataSourceType> registeredDsTypes = Sets.newHashSet();

    private static synchronized void registerDrivers() {
        try {
            File driversDir = new File(IoCommons.getAppServerDirPath() + "/wfe.data-sources/drivers");
            if (!driversDir.exists()) {
                return;
            }
            File[] drivers = driversDir.listFiles();
            if (drivers == null) {
                return;
            }
            List<URL> urls = Lists.newArrayList();
            for (File driver : drivers) {
                String driverJarName = driver.getAbsolutePath();
                if (!driverJarNames.contains(driverJarName)) {
                    urls.add(new URL(MessageFormat.format("jar:file:{0}!/", driverJarName)));
                    driverJarNames.add(driverJarName);
                }
            }
            if (urls.isEmpty()) {
                return;
            }
            URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[] {}));
            JdbcDataSourceType[] dsTypes = JdbcDataSourceType.values();
            for (JdbcDataSourceType dsType : dsTypes) {
                if (!registeredDsTypes.contains(dsType)) {
                    String driverClassName = dsType.getDriverClassName();
                    try {
                        Driver driver = (Driver) Class.forName(driverClassName, true, urlClassLoader).newInstance();
                        DriverManager.registerDriver(new DriverWrapper(driver));
                        registeredDsTypes.add(dsType);
                        log.info("JDBC-driver " + driverClassName + " registered successfully");
                    } catch (ClassNotFoundException | SQLException e) {
                        log.info("JDBC-driver " + driverClassName + " not available");
                    }
                }
            }
        } catch (Exception e) {
            log.error("registerDrivers failed", e);
        }
    }

    private static synchronized File getStorageDir() {
        if (storageDir == null) {
            storageDir = new File(IoCommons.getAppServerDirPath() + "/wfe.data-sources");
            if (!storageDir.exists()) {
                storageDir.mkdir();
            }
        }
        return storageDir;
    }

    private static synchronized File getStorageHistoryDir() {
        if (storageHistoryDir == null) {
            storageHistoryDir = new File(getStorageDir(), "history");
            if (!storageHistoryDir.exists()) {
                storageHistoryDir.mkdir();
            }
        }
        return storageHistoryDir;
    }

    private static synchronized boolean moveToHistory(File dsFile) {
        return dsFile.renameTo(new File(getStorageHistoryDir(), dsFile.getName() + TIMESTAMP_FORMAT.format(new Date())));
    }

    public static synchronized boolean moveToHistory(String dsName) {
        return moveToHistory(new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX));
    }

    public static synchronized void clear() {
        for (String dsName : getNames()) {
            moveToHistory(new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX));
        }
    }

    public static synchronized void changePassword(String dsName, String password) {
        DataSource ds = getDataSource(dsName);
        if (ds instanceof JdbcDataSource) {
            Document document = XmlUtils.parseWithoutValidation(restore(dsName));
            Element root = document.getRootElement();
            Element psw = root.element(ELEMENT_PASSWORD);
            if (psw == null) {
                root.addElement(ELEMENT_PASSWORD).addText(password);
            } else {
                psw.setText(password);
            }
            save(XmlUtils.save(document, OutputFormat.createPrettyPrint()), true, false);
        }
    }

    public static DataSource getDataSource(String dsName) {
        registerDrivers();
        Document document = XmlUtils.parseWithoutValidation(restore(dsName));
        DataSource dataSource = DataSourceCreator.create(DataSourceType.valueOf(document.getRootElement().attributeValue(ATTR_TYPE)));
        dataSource.init(document);
        return dataSource;
    }

    public static List<DataSource> getAllDataSources() {
        List<DataSource> all = Lists.newArrayList();
        for (String dsName : getNames()) {
            all.add(getDataSource(dsName));
        }
        return all;
    }

    public static List<String> getNames() {
        File[] files = getStorageDir().listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().endsWith(DATA_SOURCE_FILE_SUFFIX);
            }
        });
        List<String> names = Lists.newArrayList();
        for (File file : files) {
            String name = file.getName();
            names.add(name.substring(0, name.length() - DATA_SOURCE_FILE_SUFFIX.length()));
        }
        return names;
    }

    public static void save(byte[] content) {
        save(content, true, true);
    }

    /**
     * Saves the data source properties to the local storage.
     * 
     * @param content
     *            - the data source properties content.
     * @param force
     *            - force to overwrite if the data source already exists.
     * @param preservePassword
     *            - if true is passed then the old data source password won't be changed.
     * @return true if the method succeed, false if the data source with the given name has existed and the force argument is false.
     */
    public static boolean save(byte[] content, boolean force, boolean preservePassword) {
        Document document = XmlUtils.parseWithoutValidation(content);
        Element root = document.getRootElement();
        String dsName = root.attributeValue(ATTR_NAME);
        File dsFile = new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX);
        if (force || !dsFile.exists()) {
            byte[] contentTmp = content;
            if (dsFile.exists()) {
                if (preservePassword && root.attributeValue(ATTR_TYPE).equals(DataSourceType.JDBC.name())) {
                    String password = XmlUtils.parseWithoutValidation(restore(dsName)).getRootElement().elementText(ELEMENT_PASSWORD);
                    Element psw = root.element(ELEMENT_PASSWORD);
                    if (password == null) {
                        if (psw != null) {
                            root.remove(psw);
                        }
                    } else {
                        if (psw != null) {
                            psw.setText(password);
                        } else {
                            root.addElement(ELEMENT_PASSWORD).setText(password);
                        }
                    }
                    contentTmp = XmlUtils.save(document, OutputFormat.createPrettyPrint());
                }
                if (moveToHistory(dsFile)) {
                    dsFile = new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX);
                }
            }
            try (FileOutputStream fos = new FileOutputStream(dsFile)) {
                fos.write(contentTmp);
                return true;
            } catch (IOException e) {
                throw new InternalApplicationException(e);
            }
        } else {
            return false;
        }
    }

    public static byte[] restore(String dsName) {
        try {
            return FileCopyUtils.copyToByteArray(new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX));
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static byte[] restoreWithoutPassword(String dsName) {
        DataSource ds = getDataSource(dsName);
        if (ds instanceof JdbcDataSource) {
            Document document = XmlUtils.parseWithoutValidation(restore(dsName));
            Element root = document.getRootElement();
            Element psw = root.element(ELEMENT_PASSWORD);
            if (psw != null) {
                root.remove(psw);
            }
            return XmlUtils.save(document, OutputFormat.createPrettyPrint());
        } else {
            return restore(dsName);
        }
    }

}
