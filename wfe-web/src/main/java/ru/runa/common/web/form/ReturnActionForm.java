package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

public class ReturnActionForm extends IdForm {
    private static final long serialVersionUID = 8416054409153112757L;

    public static final String RETURN_ACTION = "returnAction";

    private String returnAction;

    public String getReturnAction() {
        return returnAction;
    }

    public void setReturnAction(String forwardName) {
        returnAction = forwardName;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (returnAction == null || returnAction.length() < 1) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
