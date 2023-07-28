package ru.runa.wf.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.definition.FileDataProvider;

/**
 * @struts:form name = "idUrlForm"
 */
public class DefinitionFileForm extends IdForm {
    private static final long serialVersionUID = 1L;

    public static final String URL_INPUT_NAME = "fileName";

    private String fileName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String url) {
        fileName = url;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        if (fileName == null) {
            fileName = FileDataProvider.INDEX_FILE_NAME;
        }
        return super.validate(mapping, request);
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        fileName = null;
    }
}
