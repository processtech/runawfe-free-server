package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.RelationForm;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.service.delegate.Delegates;

public class UpdateRelationAction extends ActionBase {
    public static final String ACTION_PATH = "/updateRelation";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        RelationForm form = (RelationForm) actionForm;
        try {
            Relation relation = Delegates.getRelationService().getRelation(getLoggedUser(request), form.getRelationId());
            relation.setName(form.getName());
            relation.setDescription(form.getDescription());
            Delegates.getRelationService().updateRelation(getLoggedUser(request), relation);
        } catch (Exception e) {
            addError(request, e);
            return mapping.findForward(Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }
}
