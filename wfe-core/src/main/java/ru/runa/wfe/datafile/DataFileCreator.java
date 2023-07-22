package ru.runa.wfe.datafile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.datafile.builder.DataFileBuilder;
import ru.runa.wfe.user.User;

/**
 * Populate zip archive.
 * 
 * @author riven
 * @author mezubarev
 */
@Component
public class DataFileCreator {
    @Autowired
    private List<DataFileBuilder> builders;

    public byte[] create(User user) {

        try { 
            File file = File.createTempFile(DataFileBuilder.FILE_NAME, DataFileBuilder.FILE_EXT);
            file.deleteOnExit();
            Document script = XmlUtils.createDocument("workflowScript", XmlUtils.RUNA_NAMESPACE);
            try (FileOutputStream fout = new FileOutputStream(file); ZipOutputStream zos = new ZipOutputStream(fout)) {
                for (DataFileBuilder builder : builders) {
                    builder.build(zos, script, user);
                }
                ZipEntry zipEntry = new ZipEntry(DataFileBuilder.PATH_TO_XML);
                zos.putNextEntry(zipEntry);
                byte[] documentInBytes = XmlUtils.save(script);
                zos.write(documentInBytes, 0, documentInBytes.length);
                zos.closeEntry();
            }
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        }
    }

    @PostConstruct
    public void init() {
        builders.sort(OrderComparator.INSTANCE);
    }
}
