package ru.runa.wf.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/undeployProcessDefinitions" name="idsForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/manage_process_definitions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definitions.do"
 *                        redirect = "true"
 */
public class UndeployProcessDefinitionsAction extends ActionBase {

    public static final String ACTION_PATH = "/undeployProcessDefinitions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        IdsForm idsForm = (IdsForm) form;
        for (Long definitionVersionId : idsForm.getIds()) {
            try {
                WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getLoggedUser(request), definitionVersionId);
                Delegates.getDefinitionService().undeployProcessDefinition(getLoggedUser(request), definition.getName(), null);
            } catch (Exception e) {
                addError(request, e);
            }
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
