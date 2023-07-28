package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.UpdateRootDigitalSignatureDetailsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.service.DigitalSignatureService;
import ru.runa.wfe.service.delegate.Delegates;

public class UpdateRootDigitalSignatureDetailsAction extends ActionBase {
    public static final String ACTION_PATH = "/updateRootDigitalSignatureDetails";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        UpdateRootDigitalSignatureDetailsForm form = (UpdateRootDigitalSignatureDetailsForm) actionForm;
        try {
            DigitalSignatureService digitalSignatureService = Delegates.getDigitalSignatureService();
            DigitalSignature rootDigitalSignature = digitalSignatureService.getRootDigitalSignature(getLoggedUser(request));
            rootDigitalSignature.setCommonName(form.getCommonName());
            rootDigitalSignature.setEmail(form.getEmail());
            rootDigitalSignature.setDepartment(form.getDepartment());
            rootDigitalSignature.setOrganization(form.getOrganization());
            rootDigitalSignature.setCity(form.getCity());
            rootDigitalSignature.setState(form.getState());
            rootDigitalSignature.setCountry(form.getCountry());
            rootDigitalSignature.setValidityInMonth(Integer.parseInt(form.getValidity()));
            rootDigitalSignature.setIssueAndExpiryDate();
            digitalSignatureService.updateRoot (getLoggedUser(request), rootDigitalSignature);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, form.getId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, form.getId());
    }
}
