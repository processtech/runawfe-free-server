package ru.runa.common.web.form;

import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

public class AdminScriptForm extends ActionForm {
    private static final long serialVersionUID = 1L;

    private boolean ajax;
    private String action;
    private String fileName;
    private String script;
    private FormFile uploadFile;

    public boolean isAjax() {
        return ajax;
    }

    public void setAjax(boolean ajax) {
        this.ajax = ajax;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public FormFile getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(FormFile uploadFile) {
        this.uploadFile = uploadFile;
    }

}
