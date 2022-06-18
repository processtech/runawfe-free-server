package ru.runa.wf.web.action;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

public class CancelProcessesAction extends ActionBase {
    public static final String ACTION_PATH = "/cancelProcesses";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        User user = getLoggedUser(request);
        List<Long> processIds = Stream.of(((IdsForm) actionForm).getIds()).distinct().collect(Collectors.toList());
        for (Long id : processIds) {
            try {
                Delegates.getExecutionService().cancelProcess(user, id);
            } catch (Exception e) {
                addError(request, e);
            }
        }
        addMessage(request, new ActionMessage(MessagesProcesses.PROCESSES_CANCELED.getKey(), processIds.size()));
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), new HashMap<>());
    }
}
