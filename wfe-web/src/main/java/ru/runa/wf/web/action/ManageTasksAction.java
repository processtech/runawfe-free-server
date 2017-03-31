package ru.runa.wf.web.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Commons;
import ru.runa.common.web.action.TabHeaderForwardAction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.user.User;

public class ManageTasksAction extends TabHeaderForwardAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User user = Commons.getUser(request.getSession());
        if (!user.getActor().isActive()) {
            List<Substitution> substitutions = Delegates.getSubstitutionService().getSubstitutions(user, user.getActor().getId());
            ActionMessages messages = getMessages(request);
            if (substitutions.isEmpty()) {
                messages.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage(MessagesExecutor.LABEL_YOU_ARE_NOT_ENABLED_WITHOUT_SUBSTITUTIONS.getKey()));
            } else {
                messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(MessagesExecutor.LABEL_YOU_ARE_NOT_ENABLED_WITH_SUBSTITUTIONS.getKey()));
            }
            saveMessages(request.getSession(), messages);
        }
        return super.execute(mapping, form, request, response);
    }

}
