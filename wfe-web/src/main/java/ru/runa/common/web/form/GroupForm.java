package ru.runa.common.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.common.web.MessagesException;

/**
 * Created on 06.07.2005
 * 
 * @struts:form name = "groupForm"
 */
public class GroupForm extends AbstractBatchPresentationForm {
    private static final long serialVersionUID = -1913069939336856450L;

    public static final String GROUP_ID = "groupId";

    public static final String GROUP_ACTION_ID = "actionId";

    public static final String GROUP_ACTION_COLLAPSE = "collapse";

    public static final String GROUP_ACTION_EXPAND = "expand";

    private String groupId;

    private String actionId;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (getGroupId() == null) {
            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesException.ERROR_NULL_VALUE.getKey()));
        }
        return errors;
    }
}
