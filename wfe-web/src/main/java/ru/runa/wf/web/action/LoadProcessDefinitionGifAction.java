package ru.runa.wf.web.action;

/**
 * Powered by Dofs
 */
abstract class LoadProcessDefinitionGifAction extends LoadProcessDefinitionFileAction {
    protected String getContentType() {
        return "image/gif";
    }
}
