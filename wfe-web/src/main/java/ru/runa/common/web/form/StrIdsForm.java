package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

/**
 * Created on 18.08.2004
 * 
 * @struts:form name = "strIdsForm"
 */
public class StrIdsForm extends ActionForm {

    private static final long serialVersionUID = 5090877167080528466L;

    public static final String IDS_INPUT_NAME = "strIds";

    private String[] strIds;

    public String[] getStrIds() {
        return strIds.clone();
    }

    public void setStrIds(String[] strIds) {
        this.strIds = strIds.clone();
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        strIds = new String[0];
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (strIds == null) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
