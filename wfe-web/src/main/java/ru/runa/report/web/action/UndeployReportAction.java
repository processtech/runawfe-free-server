package ru.runa.report.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.10.2004
 * 
 * @struts:action path="/undeployReport" name="idsForm" validate="false"
 * @struts.action-forward name="success" path="/manage_reports.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_reports.do" redirect = "true"
 */
public class UndeployReportAction extends ActionBase {

    public static final String ACTION_PATH = "/undeployReport";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        IdsForm idsForm = (IdsForm) form;
        for (Long id : idsForm.getIds()) {
            try {
                Delegates.getReportService().undeployReport(getLoggedUser(request), id);
            } catch (Exception e) {
                addError(request, e);
            }
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
