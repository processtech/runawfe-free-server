package ru.runa.common.web;

import java.util.LinkedHashMap;
import javax.servlet.jsp.PageContext;
import org.apache.struts.action.ActionForward;
import ru.runa.af.web.form.GrantPermissionsForm;
import ru.runa.wfe.security.SecuredObjectType;

public class PermissionWebUtils {

    /**
     * Common code for {@link ru.runa.af.web.action.GrantPermissionsAction} and {@link ru.runa.af.web.action.UpdatePermissionsAction}.
     * Both must return to permission editor form: either /managePermissionsForm or embedded form on other page (e.g. /manage_system).
     * <p>
     * For this reason UpdatePermissionsForm is inherited from GrantPermissionsForm: they have common fields required by this method.
     * (Ugly, but I don't want separate base class and empty GrantPermissionsForm subclass.)
     */
    public static ActionForward getReturnActionForward(GrantPermissionsForm form) {
        String returnAction;
        if (form.getReturnAction() != null) {
            returnAction = form.getReturnAction();
        } else {
            // Better return somewhere than nowhere. So we return to standalone permission editor form.
            StringBuilder sb = new StringBuilder();
            sb.append("/managePermissionsForm.do?type=").append(form.getSecuredObjectType());
            if (form.getId() != null && form.getId() != 0) {
                sb.append("&id=").append(form.getId());
            }
            returnAction = sb.toString();
        }
        return new ActionForward(returnAction, true);
    }

    /**
     * Common code for {@link ru.runa.common.web.tag.GrantPermissionsLinkTag} and {@link ru.runa.common.web.tag.ManagePermissionsLinkTag}.
     */
    public static LinkedHashMap<String, Object> getLinkHRefParams(PageContext cfx, SecuredObjectType type, Long id) {
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("type", type.getName());
        if (id != null && id != 0) {
            params.put("id", id);
        }
        params.put("return", Commons.getSelfActionWithQueryString(cfx));
        return params;
    }
}
