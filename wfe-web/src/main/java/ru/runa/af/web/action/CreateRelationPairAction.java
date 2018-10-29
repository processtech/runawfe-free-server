package ru.runa.af.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.af.web.form.RelationPairForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

public class CreateRelationPairAction extends ActionBase {
    public static final String VIEW_PATH = "create_relation_pair.do";
    public static final String ACTION_PATH = "/createRelationPair";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        RelationPairForm relationForm = (RelationPairForm) form;
        try {
            Executor executorFrom = Delegates.getExecutorService().getExecutorByName(getLoggedUser(request), relationForm.getExecutorFrom());
            Executor executorTo = Delegates.getExecutorService().getExecutorByName(getLoggedUser(request), relationForm.getExecutorTo());
            Delegates.getRelationService().addRelationPair(getLoggedUser(request), relationForm.getRelationId(), executorFrom, executorTo);
        } catch (Exception e) {
            addError(request, e);
            return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), RelationPairForm.RELATION_ID, relationForm.getRelationId());
        }
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), RelationPairForm.RELATION_ID, relationForm.getRelationId());
    }

}
