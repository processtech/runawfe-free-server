package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.RestoreDefaultSettingValueForm;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

public class RestoreDefaultSettingAction extends ActionBase {
    public static final String RESTORE_SETTINGS_ACTION_PATH = "/restore_setting";
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        if (!Delegates.getExecutorService().isAdministrator(Commons.getUser(request.getSession())))
            throw new AuthorizationException("No permission on this page");
        try {
            RestoreDefaultSettingValueForm pform = (RestoreDefaultSettingValueForm) form;
            String settingName = pform.getSettingName();
            PropertyResources resource = new PropertyResources(pform.getFileName());
            Long settingIdentifier = resource.getIdentifier(settingName);
            if (settingIdentifier != null) {
                ApplicationContextFactory.getSettingDAO().delete(settingIdentifier);
            }
            resource.removeCachedProperty(settingName);
        } catch (Exception e) {
            log.error("", e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
