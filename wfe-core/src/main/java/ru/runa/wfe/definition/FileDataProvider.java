package ru.runa.wfe.definition;

public interface FileDataProvider {
    String PAR_FILE = "par";
    String REPORT_FILE = "jasper";
    String PROCESSDEFINITION_XML_FILE_NAME = "processdefinition.xml";
    String FORMS_XML_FILE_NAME = "forms.xml";
    String GPD_XML_FILE_NAME = "gpd.xml";
    String VARIABLES_XML_FILE_NAME = "variables.xml";
    String GRAPH_IMAGE_OLD1_FILE_NAME = "graph.gif";
    String GRAPH_IMAGE_OLD2_FILE_NAME = "processimage.jpg";
    String GRAPH_IMAGE_NEW_FILE_NAME = "processimage.png";
    String INDEX_FILE_NAME = "index.html";
    String START_DISABLED_IMAGE_FILE_NAME = "start-disabled.png";
    String START_IMAGE_FILE_NAME = "start.png";
    String FORM_CSS_FILE_NAME = "form.css";
    String FORM_JS_FILE_NAME = "form.js";
    String SUBSTITUTION_EXCEPTIONS_FILE_NAME = "substitutionExceptions.xml";
    String BOTS_XML_FILE = "bots.xml";
    String PROCESS_FILE_PROTOCOL = "processfile://";
    String BOT_TASK_FILE_PROTOCOL = "botfile://";
    String SUBPROCESS_DEFINITION_PREFIX = "sub";
    String COMMENTS_XML_FILE_NAME = "comments.xml";

    byte[] getFileData(String fileName);
    byte[] getFileDataNotNull(String fileName);
}
