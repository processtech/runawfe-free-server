package ru.runa.af.web.form;

import ru.runa.common.web.form.IdsForm;

/**
 * @struts:form name = "grantPermissionsForm"
 */
public class GrantPermissionsForm extends IdsForm {
    private static final long serialVersionUID = 1L;

    private String securedObjectType;
    private String returnAction;

    public String getSecuredObjectType() {
        return securedObjectType;
    }

    public void setSecuredObjectType(String securedObjectType) {
        this.securedObjectType = securedObjectType;
    }

    public String getReturnAction() {
        return returnAction;
    }

    public void setReturnAction(String returnAction) {
        this.returnAction = returnAction;
    }
}
