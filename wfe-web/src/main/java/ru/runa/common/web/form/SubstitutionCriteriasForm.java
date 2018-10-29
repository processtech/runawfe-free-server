package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

/**
 * @struts:form name = "substitutionCriteriasForm"
 */
public class SubstitutionCriteriasForm extends ActionForm {

    private static final long serialVersionUID = 57003922541843531L;

    public static final String IDS_INPUT_NAME = "ids";
    public static final String REMOVE_METHOD_INPUT_NAME = "removeMethod";

    public static final String REMOVE_METHOD_CONFIRM = "confirm";
    public static final String REMOVE_METHOD_ALL = "all";
    public static final String REMOVE_METHOD_ONLY = "only";

    private Long[] ids;
    private String removeMethod;

    public Long[] getIds() {
        return ids.clone();
    }

    public void setIds(Long[] ids) {
        this.ids = ids.clone();
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        ids = new Long[0];
    }

    public String getRemoveMethod() {
        return removeMethod;
    }

    public void setRemoveMethod(String removeMethod) {
        this.removeMethod = removeMethod;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (ids == null || getRemoveMethod() == null || getRemoveMethod().isEmpty()) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
