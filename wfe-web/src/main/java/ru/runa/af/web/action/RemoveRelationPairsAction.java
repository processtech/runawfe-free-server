package ru.runa.af.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.RelationPairForm;
import ru.runa.af.web.form.RelationPairsForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

public class RemoveRelationPairsAction extends ActionBase {
    public static final String ACTION_PATH = "/removeRelationPairs";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce) {
        RelationPairsForm relationForm = (RelationPairsForm) form;
        try {
            List<Long> ids = Lists.newArrayList(relationForm.getIds());
            Delegates.getRelationService().removeRelationPairs(getLoggedUser(request), ids);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), RelationPairForm.RELATION_ID, relationForm.getRelationId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), RelationPairForm.RELATION_ID, relationForm.getRelationId());
    }

}
