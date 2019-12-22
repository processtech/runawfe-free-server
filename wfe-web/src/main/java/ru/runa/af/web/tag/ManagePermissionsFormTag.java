package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.UpdatePermissionsAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag2;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "managePermissionsForm")
public class ManagePermissionsFormTag extends SecuredObjectFormTag2 {
    private static final long serialVersionUID = 1L;

    @Override
    public String getAction() {
        return UpdatePermissionsAction.ACTION_PATH;
    }

    @Override
    public final void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.READ_PERMISSIONS, getSecuredObject());
        super.fillFormElement(tdFormElement);

        // This is for UpdatePermissionsAction to return back to current page:
        tdFormElement.addElement(new Input(Input.HIDDEN, "returnAction", Commons.getSelfActionWithQueryString(pageContext)));

        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(getSecuredObject(), getUser(), pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        StrutsMessage messagesCommon = securedObjectType == SecuredObjectType.EXECUTORS ?
            MessagesCommon.TITLE_EXECUTORS_PERMISSIONS :
            MessagesCommon.TITLE_PERMISSION_OWNERS;

        return messagesCommon.message(pageContext);
    }
}
