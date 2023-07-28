package ru.runa.wf.web.action;

import java.util.List;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/deployProcessDefinition" name="fileForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/manage_process_definitions.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/deploy_process_definition.do"
 *                        redirect = "false"
 */
public class DeployProcessDefinitionAction extends BaseDeployProcessDefinitionAction {
    public static final String ACTION_PATH = "/deployProcessDefinition";

    @Override
    protected void doAction(User user, FileForm fileForm, boolean isUpdateCurrentVersion, List<String> categories, Integer secondsBeforeArchiving)
            throws Exception {
        Delegates.getDefinitionService().deployProcessDefinition(user, fileForm.getFile().getFileData(), categories, secondsBeforeArchiving);
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_SUCCESS);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return mapping.findForward(Resources.FORWARD_FAILURE);
    }

    @Override
    protected void prepare(FileForm fileForm) {
    }
}
