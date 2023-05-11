package ru.runa.af.web.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.UpdateDigitalSignatureDetailsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.service.DigitalSignatureService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.digitalsignature.DigitalSignature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @struts:action path="/updateDigitalSignatureDetails" name="updateDigitalSignatureDetailsForm" validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/manage_executor.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executor.do" redirect = "true"
 */
public class UpdateDigitalSignatureDetailsAction extends ActionBase {

    public static final String ACTION_PATH = "/updateDigitalSignatureDetails";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdateDigitalSignatureDetailsForm form = (UpdateDigitalSignatureDetailsForm) actionForm;
        try {
            DigitalSignatureService digitalSignatureService = Delegates.getDigitalSignatureService();
            DigitalSignature digitalSignature = digitalSignatureService.getDigitalSignature(getLoggedUser(request),
                    Long.parseLong(form.getExecutorId()));
            digitalSignature.setCommonName(form.getCommonName());
            digitalSignature.setEmail(form.getEmail());
            digitalSignature.setDepartment(form.getDepartment());
            digitalSignature.setOrganization(form.getOrganization());
            digitalSignature.setCity(form.getCity());
            digitalSignature.setState(form.getState());
            digitalSignature.setCountry(form.getCountry());
            digitalSignature.setValidityInMonth(Integer.parseInt(form.getValidity()));
            digitalSignature.setIssueAndExpiryDate();
            digitalSignatureService.update(getLoggedUser(request), digitalSignature);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }
}
