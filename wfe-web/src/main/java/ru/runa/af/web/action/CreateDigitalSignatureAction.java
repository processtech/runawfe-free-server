package ru.runa.af.web.action;

import org.apache.ecs.vxml.If;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.af.web.form.CreateDigitalSignatureForm;
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
 * Created on 25.08.2022
 *
 * @struts:action path="/createDigitalSignature" name="createDigitalSignatureForm" validate="true" input = "/WEB-INF/af/create_digital_signature.jsp"
 * @struts.action-forward name="success" path="/manage_executors.do" redirect = "true"
 * @struts.action-forward name="failure" path="/manage_executors.do" redirect = "true"
 */
public class CreateDigitalSignatureAction extends ActionBase {
    public static final String ACTION_PATH = "/createDigitalSignature";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        CreateDigitalSignatureForm createForm = (CreateDigitalSignatureForm) form;
        try {
            DigitalSignatureService digitalSignatureService = Delegates.getDigitalSignatureService();
            if (!digitalSignatureService.doesRootDigitalSignatureExist(getLoggedUser(request))){
                throw new Exception("Root digital signature doesn't exist");
            }
            DigitalSignature digitalSignature = digitalSignatureService.create(getLoggedUser(request),
                    new DigitalSignature(createForm.getCommonName(),
                            createForm.getEmail(),
                            createForm.getDepartment(),
                            createForm.getOrganization(),
                            createForm.getCity(),
                            createForm.getState(),
                            createForm.getCountry(),
                            Integer.parseInt(createForm.getValidity()),
                            Long.parseLong(createForm.getExecutorId())));
            digitalSignatureService.update(getLoggedUser(request), digitalSignature);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME,
                    createForm.getExecutorId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, createForm.getExecutorId());
    }
}
