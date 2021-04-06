package ru.runa.af.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Created on 03.02.2006
 * 
 * @struts:action path="/switchSubstitutionsPositions" name="idsForm"
 *                validate="true" input = "/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="success" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure" path="/WEB-INF/af/manage_executor.jsp"
 * @struts.action-forward name="failure_executor_does_not_exist"
 *                        path="/WEB-INF/af/manage_executors.jsp"
 */

public class SwitchSubstitutionsPositionsAction extends ActionBase {

    public static final String ACTION_PATH = "/switchSubstitutionsPositions";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdsForm form = (IdsForm) actionForm;
        try {
            List<Substitution> substitutions = Delegates.getSubstitutionService().getSubstitutions(getLoggedUser(request), form.getId());
            Substitution substitution1 = null;
            for (Substitution substitution : substitutions) {
                if (Objects.equal(form.getIds()[0], substitution.getId())) {
                    substitution1 = substitution;
                    break;
                }
            }
            Preconditions.checkNotNull(substitution1, "No substitution found");
            Substitution substitution2 = substitutions.get(substitutions.indexOf(substitution1) + 1);
            substitution1.setPosition(substitution2.getPosition());
            Delegates.getSubstitutionService().updateSubstitution(getLoggedUser(request), substitution1);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

}
