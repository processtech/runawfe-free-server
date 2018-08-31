package ru.runa.af.web.action;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.SettingsFileForm;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author petrmikheev
 * 
 * @struts:action path="/save_settings" name="settingsFileForm" validate="false" input =
 *                "/WEB-INF/wf/edit_settings.jsp"
 */
public class SaveSettingsAction extends ActionBase {

    public static final String SAVE_SETTINGS_ACTION_PATH = "/save_settings";
    private static final Log log = LogFactory.getLog(SaveSettingsAction.class);

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
    	if (!Delegates.getExecutorService().isAdministrator(getLoggedUser(request))) {
            throw new AuthorizationException("No permission on this page");
        }
        try {
            SettingsFileForm pform = (SettingsFileForm) form;
            String resource = pform.getResource();
            Map<String, String> properties = pform.getModifiedSettings();
            SystemService service = Delegates.getSystemService();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
            	log.info(resource + "[" + entry.getKey() + "] = " + entry.getValue());
            	service.setSetting(resource, entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
