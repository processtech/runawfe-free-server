package ru.runa.wf.web.datafile.builder;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.user.User;

public class DataSourceDataFileBuilder implements DataFileBuilder {

    protected final Log log = LogFactory.getLog(getClass());
    private final User user;

    public DataSourceDataFileBuilder(User user) {
        this.user = user;
    }

    @Override
    public void build(ZipOutputStream zos, Document script) throws Exception {
        for (String dsName : DataSourceStorage.getNames()) {
            String fileName = dsName + ".xml";
            ZipEntry zipEntry = new ZipEntry(PATH_TO_DATA_SOURCE + fileName);
            zos.putNextEntry(zipEntry);
            zos.write(DataSourceStorage.restoreWithoutPassword(dsName));
            zos.closeEntry();
            Element element = script.getRootElement().addElement("deployDataSource", XmlUtils.RUNA_NAMESPACE);
            element.addAttribute("file", PATH_TO_DATA_SOURCE + fileName);
        }
    }

}
