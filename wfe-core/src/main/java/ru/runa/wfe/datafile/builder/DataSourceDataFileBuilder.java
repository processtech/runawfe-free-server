package ru.runa.wfe.datafile.builder;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.user.User;

@Component
public class DataSourceDataFileBuilder implements DataFileBuilder {

    @Override
    public void build(ZipOutputStream zos, Document script, User user) throws IOException {
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

    @Override
    public int getOrder() {
        return 6;
    }

}
