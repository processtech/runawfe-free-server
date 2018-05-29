package ru.runa.wfe.datasource;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.springframework.util.FileCopyUtils;

import com.google.common.collect.Lists;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.commons.xml.XmlUtils;

public class DataSourceStorage implements DataSourceStuff {

    private static final DateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(".yyyyMMddHHmmss");

    private static File storageDir;
    private static File storageHistoryDir;

    private static synchronized File getStorageDir() {
        if (storageDir == null) {
            storageDir = new File(IOCommons.getAppServerDirPath() + "/wfe.data-sources");
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
            save(XmlUtils.save(document, OutputFormat.createPrettyPrint()));
        }
    }

    public static DataSource getDataSource(String dsName) {
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
        Document document = XmlUtils.parseWithoutValidation(content);
        String dsName = document.getRootElement().attributeValue(ATTR_NAME);
        File dsFile = new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX);
        if (dsFile.exists()) {
            if (moveToHistory(dsFile)) {
                dsFile = new File(getStorageDir(), dsName + DATA_SOURCE_FILE_SUFFIX);
            }
        }
        try (FileOutputStream fos = new FileOutputStream(dsFile)) {
            fos.write(content);
        } catch (IOException e) {
            throw new InternalApplicationException(e);
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
