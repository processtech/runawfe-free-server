package ru.runa.wf.web.action;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.tag.RedeployDefinitionFormTag;
import ru.runa.wfe.user.User;

import static ru.runa.wf.web.tag.RedeployDefinitionFormTag.TYPE_DAYS_BEFORE_ARCHIVING;

public abstract class BaseDeployProcessDefinitionAction extends ActionBase {

    protected abstract void doAction(User user, FileForm fileForm, boolean isUpdateCurrentVersion, List<String> categories,
            Integer secondsBeforeArchiving) throws Exception;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        boolean isUpdateCurrentVersion = request.getParameter(RedeployDefinitionFormTag.TYPE_UPDATE_CURRENT_VERSION) != null;

        String s = request.getParameter(TYPE_DAYS_BEFORE_ARCHIVING);
        Integer secondsBeforeArchiving = StringUtils.isBlank(s) ? null : (int)(Double.parseDouble(s) * 86400);
        if (secondsBeforeArchiving == null) {
            // API does not change current value if we pass null;
            // but form will show current value and user can edit / delete it, so if user deleted value, we must delete it too.
            secondsBeforeArchiving = -1;
        }

        FileForm fileForm = (FileForm) form;
        prepare(fileForm);
        try {
            doAction(getLoggedUser(request), fileForm, isUpdateCurrentVersion, CategoriesSelectUtils.extract(request), secondsBeforeArchiving);
        } catch (Exception e) {
            addError(request, e);
            return getErrorForward(mapping);
        }
        return getSuccessAction(mapping);
    }

    protected abstract ActionForward getSuccessAction(ActionMapping mapping);

    protected abstract ActionForward getErrorForward(ActionMapping mapping);

    protected abstract void prepare(FileForm fileForm);
}
