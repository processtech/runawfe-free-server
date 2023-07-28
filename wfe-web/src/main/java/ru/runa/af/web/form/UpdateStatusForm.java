package ru.runa.af.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.form.IdForm;

/**
 * 
 * Created on Mar 2, 2006
 * 
 * 
 * @struts:form name = "updateStatusForm"
 */
public class UpdateStatusForm extends IdForm {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public static final String IS_ACTIVE_INPUT_NAME = "active";

    private boolean active;

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
