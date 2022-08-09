package ru.runa.wfe.datafile.builder;

import java.io.IOException;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.springframework.core.Ordered;
import ru.runa.wfe.user.User;

/**
 * Provide method creating the part of data file.
 * 
 * @author riven
 * 
 */
public interface DataFileBuilder extends Ordered {

    String FILE_NAME = "archive";
    String FILE_EXT = ".datafile";
    String PATH_TO_XML = "scripts/data.xml";
    String PATH_TO_BOTTASK = "scripts/";
    String PATH_TO_PROCESS_DEF = "processes/";
    String PATH_TO_DATA_SOURCE = "data-sources/";
    String PATH_TO_REPORTS = "reports/";

    /**
     * put data to zip archive and populate script file xml elements
     * 
     * @param zos
     *            - zip archive
     * @param script
     *            - xml file contains action for invocation
     * @param user
     *            - current actor {@linkplain User}.
     */
    void build(ZipOutputStream zos, Document script, User user) throws IOException;
}
