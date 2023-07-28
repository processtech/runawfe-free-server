package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;

/**
 * Powered by Dofs
 * 
 * @struts:action path="/processDefinitionDescription" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process_definitions.jsp"
 */
public class ProcessDefinitionDescriptionAction extends LoadProcessDefinitionFileAction {

    public static final String ACTION_PATH = "/processDefinitionDescription";

    public static final String DESCRIPTION_FILE_NAME = "description";

    protected String getFileName(HttpServletRequest request) {
        return DESCRIPTION_FILE_NAME;
    }

    protected String getContentType() {
        return "text/html";
    }
}
