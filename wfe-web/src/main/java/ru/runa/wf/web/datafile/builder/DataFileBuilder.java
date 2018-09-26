package ru.runa.wf.web.datafile.builder;

import java.util.zip.ZipOutputStream;
import org.dom4j.Document;

/**
 * Provide method creating the part of data file.
 * 
 * @author riven
 * 
 */
public interface DataFileBuilder {

    String FILE_NAME = "archive";
    String FILE_EXT = ".datafile";
    String PATH_TO_XML = "scripts/data.xml";
    String PATH_TO_BOTTASK = "scripts/";
    String PATH_TO_PROCESS_DEF = "processes/";
    String PATH_TO_DATA_SOURCE = "data-sources/";

    /**
     * put data to zip archive and populate script file xml elements
     * 
     * @param zos
     *            - zip archive
     * @param script
     *            - xml file contains action for invocation
     */
    void build(ZipOutputStream zos, Document script) throws Exception;
}
