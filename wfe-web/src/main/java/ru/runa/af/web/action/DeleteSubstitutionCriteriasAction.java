package ru.runa.af.web.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.SubstitutionCriteriasForm;
import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;

import com.google.common.collect.Lists;

/**
 * Created on 14.08.2010
 * 
 * @struts:action path="/deleteSubstitutionCriterias"
 *                name="substitutionCriteriasForm" validate="false"
 * @struts.action-forward name="success" path="/manage_system.do"
 * @struts.action-forward name="failure" path="/manage_system.do"
 */
public class DeleteSubstitutionCriteriasAction extends ActionBase {

    public static final String ACTION_PATH = "/deleteSubstitutionCriterias";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        try {
            SubstitutionService substitutionService = Delegates.getSubstitutionService();
            ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
            Long ids[] = ((SubstitutionCriteriasForm) form).getIds();
            String method = ((SubstitutionCriteriasForm) form).getRemoveMethod();
            List<SubstitutionCriteria> criterias = Lists.newArrayList();
            for (Long id : ids) {
                SubstitutionCriteria criteria = substitutionService.getCriteria(getLoggedUser(request), id);
                substitutions.addAll(substitutionService.getSubstitutionsByCriteria(getLoggedUser(request), criteria));
                criterias.add(criteria);
            }

            if (SubstitutionCriteriasForm.REMOVE_METHOD_CONFIRM.equals(method) && !substitutions.isEmpty()) {
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "substitutionCriteriaIds", Arrays.toString(ids));
            }

            if (SubstitutionCriteriasForm.REMOVE_METHOD_ALL.equals(method)) {
                List<Long> substitutionIds = Lists.newArrayList();
                for (Substitution substitution : substitutions) {
                    substitutionIds.add(substitution.getId());
                }
                substitutionService.deleteSubstitutions(getLoggedUser(request), substitutionIds);
            } else if (SubstitutionCriteriasForm.REMOVE_METHOD_ONLY.equals(method)) {
                for (Substitution substitution : substitutions) {
                    substitution.setCriteria(null);
                    substitutionService.updateSubstitution(getLoggedUser(request), substitution);
                }
            }
            substitutionService.deleteCriterias(getLoggedUser(request), criterias);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }

}
