package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

/**
 * Created on 18.08.2004
 * 
 * @struts:form name = "idsForm"
 */
public class IdsForm extends IdForm {

    private static final long serialVersionUID = 5090877167080528466L;

    public static final String IDS_INPUT_NAME = "ids";

    private Long[] ids;

    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        ids = new Long[0];
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
        if (ids == null) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
