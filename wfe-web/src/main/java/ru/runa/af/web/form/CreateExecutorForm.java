package ru.runa.af.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

/*
 * Created on 20.08.2004
 */
/**
 * @struts:form name = "createExecutorForm"
 */
public class CreateExecutorForm extends UpdateExecutorDetailsForm {

    private static final long serialVersionUID = 2563437284265955525L;

    public static final String EXECUTOR_TYPE_INPUT_NAME = "executorType";

    private String executorType;

    public static final String TYPE_GROUP = "group";

    public static final String TYPE_ACTOR = "actor";

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (getExecutorType() == null || getExecutorType().length() < 1) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        if (getExecutorType() != null && !CreateExecutorForm.TYPE_ACTOR.equals(getExecutorType())
                && !CreateExecutorForm.TYPE_GROUP.equals(getExecutorType())) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.EXCEPTION_UNKNOWN.getKey(), "Unknown type "
                    + getExecutorType()));
        }
        return errors;
    }

    public String getExecutorType() {
        return executorType;
    }

    public void setExecutorType(String string) {
        executorType = string;
    }
}
