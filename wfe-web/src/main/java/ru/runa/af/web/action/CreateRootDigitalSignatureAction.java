package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.CreateRootDigitalSignatureForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.service.DigitalSignatureService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.06.2023
 *
 * @struts:action path="/createRootDigitalSignature" name="createRootDigitalSignatureForm" validate="true" input = "/WEB-INF/af/create_root_digital_signature.jsp"
 * @struts.action-forward name="success" path="/manage_system.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_system.do" redirect = "true"
 */

public class CreateRootDigitalSignatureAction extends ActionBase {

public static final String ACTION_PATH = "/createRootDigitalSignature";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        CreateRootDigitalSignatureForm createForm = (CreateRootDigitalSignatureForm) form;
        try {
            DigitalSignatureService digitalSignatureService = Delegates.getDigitalSignatureService();
            DigitalSignature digitalSignature = digitalSignatureService.createRoot(getLoggedUser(request),
                    new DigitalSignature(createForm.getCommonName(),
                            createForm.getEmail(),
                            createForm.getDepartment(),
                            createForm.getOrganization(),
                            createForm.getCity(),
                            createForm.getState(),
                            createForm.getCountry(),
                            Integer.parseInt(createForm.getValidity()),
                            0L));
            digitalSignatureService.updateRoot(getLoggedUser(request), digitalSignature);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "","");
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), "", "");
    }
}
