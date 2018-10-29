package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;

import ru.runa.wfe.definition.FileDataProvider;

/**
 * 
 * @struts:action path="/startDisabledImageProcess" name="idForm" validate="true" input = "/WEB-INF/wf/manage_process_definitions.jsp"
 * 
 * 
 *                Created on 24.10.2006
 */
public class StartDisabledImageProcessAction extends LoadProcessDefinitionGifAction {
    public static final String ACTION_PATH = "/startDisabledImageProcess";

    @Override
    protected String getFileName(HttpServletRequest request) {
        return FileDataProvider.START_DISABLED_IMAGE_FILE_NAME;
    }
}
