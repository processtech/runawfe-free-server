package ru.runa.af.web.action;

import com.google.common.collect.Lists;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.GrantPermissionsForm;
import ru.runa.common.web.PermissionWebUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/grantPermissions" name="grantPermissionsForm" validate="false"
 */
public class GrantPermissionsAction extends ActionBase {

    public static final String ACTION_PATH = "/grantPermissions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        GrantPermissionsForm form = (GrantPermissionsForm) actionForm;
        List<Long> ids = Lists.newArrayList(form.getIds());
        try {
            SecuredObjectType securedObjectType = SecuredObjectType.valueOf(form.getSecuredObjectType());
            SecuredObject object = Delegates.getAuthorizationService().findSecuredObject(securedObjectType, form.getId());
            Delegates.getAuthorizationService().setPermissions(getLoggedUser(request), ids, ApplicablePermissions.getDefaults(object), object);
        } catch (Exception e) {
            addError(request, e);
        }

        return PermissionWebUtils.getReturnActionForward(form);
    }
}
