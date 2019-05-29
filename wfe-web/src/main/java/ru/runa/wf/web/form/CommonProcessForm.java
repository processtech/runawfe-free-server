package ru.runa.wf.web.form;

import ru.runa.common.web.form.IdForm;

/**
 * @struts:form name = "commonProcessForm"
 */
public class CommonProcessForm extends IdForm {
    private static final long serialVersionUID = -4345644809568995336L;

    private String submitButton;

    private boolean multipleSubmit;

    public String getSubmitButton() {
        if (multipleSubmit) {
            return submitButton;
        }
        return null;
    }

    public void setSubmitButton(String submitButton) {
        this.submitButton = submitButton;
    }

    public void setMultipleSubmit(boolean multipleSubmit) {
        this.multipleSubmit = multipleSubmit;
    }
}
