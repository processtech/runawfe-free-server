package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.DeployDataSourceForm;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @struts:action path="/deploy_data_source" name="deployDataSourceForm" validate="false"
 */
public class DeployDataSourceAction extends ActionBase {

    public static final String ACTION_PATH = "/deploy_data_source";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) {
        DeployDataSourceForm fileForm = (DeployDataSourceForm) form;
        try {
            byte[] data = fileForm.getFile().getFileData();
            if (data != null && data.length > 0) {
                Delegates.getDataSourceService().importDataSource(getLoggedUser(request), data);
            }
        } catch (Exception e) {
            addError(request, e);
        }
        return new ActionForward("/manage_data_sources.do");
    }

}
