package ru.runa.wfe.definition;

public interface IFileDataProvider {
    public static final String PAR_FILE = "par";
    public static final String PROCESSDEFINITION_XML_FILE_NAME = "processdefinition.xml";
    public static final String FORMS_XML_FILE_NAME = "forms.xml";
    public static final String GPD_XML_FILE_NAME = "gpd.xml";
    public static final String VARIABLES_XML_FILE_NAME = "variables.xml";
    public static final String GRAPH_IMAGE_OLD1_FILE_NAME = "graph.gif";
    public static final String GRAPH_IMAGE_OLD2_FILE_NAME = "processimage.jpg";
    public static final String GRAPH_IMAGE_NEW_FILE_NAME = "processimage.png";
    public static final String INDEX_FILE_NAME = "index.html";
    public static final String START_DISABLED_IMAGE_FILE_NAME = "start-disabled.png";
    public static final String START_IMAGE_FILE_NAME = "start.png";
    public static final String FORM_CSS_FILE_NAME = "form.css";
    public static final String FORM_JS_FILE_NAME = "form.js";
    public static final String SUBSTITUTION_EXCEPTIONS_FILE_NAME = "substitutionExceptions.xml";
    public static final String BOTS_XML_FILE = "bots.xml";
    public static final String PROCESS_FILE_PROTOCOL = "processfile://";
    public static final String BOT_TASK_FILE_PROTOCOL = "botfile://";
    public static final String SUBPROCESS_DEFINITION_PREFIX = "sub";
    public static final String COMMENTS_XML_FILE_NAME = "comments.xml";
    public static final String REGULATIONS_HTML_FILE_NAME = "regulations.html";

    byte[] getFileData(String fileName);

    byte[] getFileDataNotNull(String fileName);

}
