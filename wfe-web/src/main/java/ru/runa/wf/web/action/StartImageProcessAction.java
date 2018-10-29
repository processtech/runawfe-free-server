package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;

import ru.runa.wfe.definition.FileDataProvider;

/**
 * Powered by Dofs
 * 
 * @struts:action path="/startImageProcess" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process_definitions.jsp"
 */
public class StartImageProcessAction extends LoadProcessDefinitionGifAction {
    public static final String ACTION_PATH = "/startImageProcess";

    @Override
    protected String getFileName(HttpServletRequest request) {
        return FileDataProvider.START_IMAGE_FILE_NAME;
    }
}
