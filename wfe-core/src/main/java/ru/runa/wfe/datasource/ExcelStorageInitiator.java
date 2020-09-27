package ru.runa.wfe.datasource;

import java.io.File;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import ru.runa.wfe.commons.IoCommons;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * @author Alekseev Mikhail
 * @since #1766
 */
@CommonsLog
public class ExcelStorageInitiator {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static synchronized byte[] init() {
        if (DataSourceStorage.getNames().contains(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME)) {
            return DataSourceStorage.restore(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);
        }

        final String excelStorageDirPath = IoCommons.getExcelStorageDirPath();
        new File(excelStorageDirPath).mkdir();
        log.info("Created " + excelStorageDirPath);

        final Document document = DocumentHelper.createDocument();
        document.addElement(DataSourceStuff.ELEMENT_DATA_SOURCE)
                .addAttribute(DataSourceStuff.ATTR_NAME, DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME)
                .addAttribute(DataSourceStuff.ATTR_TYPE, DataSourceType.Excel.name())
                .addElement(DataSourceStuff.ELEMENT_FILE_PATH).addText(excelStorageDirPath);
        final byte[] internalStorageDs = XmlUtils.save(document, OutputFormat.createPrettyPrint());
        DataSourceStorage.save(internalStorageDs, true, false);
        log.info(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME + " is saved");

        return internalStorageDs;
    }
}
