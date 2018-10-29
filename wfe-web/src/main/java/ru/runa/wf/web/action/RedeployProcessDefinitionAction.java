package ru.runa.wf.web.action;

import com.google.common.base.Strings;
import java.io.IOException;
import java.util.List;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 06.10.2004
 *
 * @struts:action path="/redeployProcessDefinition" name="fileForm"
 *                validate="false"
 * @struts.action-forward name="success" path="/manage_process_definition.do"
 *                        redirect = "true"
 * @struts.action-forward name="failure" path="/manage_process_definition.do"
 *                        redirect = "false"
 * @struts.action-forward name="failure_process_definition_does_not_exist"
 *                        path="/manage_process_definitions.do" redirect =
 *                        "true"
 */
public class RedeployProcessDefinitionAction extends BaseDeployProcessDefinitionAction {
    public static final String ACTION_PATH = "/redeployProcessDefinition";

    private Long definitionVersionId;

    @Override
    protected void doAction(User user, FileForm fileForm, boolean isUpdateCurrentVersion, List<String> categories, Integer secondsBeforeArchiving)
            throws IOException {
        byte[] data = Strings.isNullOrEmpty(fileForm.getFile().getFileName()) ? null : fileForm.getFile().getFileData();
        Long definitionVersionId = fileForm.getId();
        if (secondsBeforeArchiving == null) {
            // API does not change current value if we pass null;
            // but form will show current value and user can edit / delete it, so if user deleted value, we must delete it too.
            secondsBeforeArchiving = -1;
        }
        WfDefinition definition = isUpdateCurrentVersion
                ? Delegates.getDefinitionService().updateProcessDefinition(user, definitionVersionId, data)
                : Delegates.getDefinitionService().redeployProcessDefinition(user, definitionVersionId, data, categories, secondsBeforeArchiving);
        this.definitionVersionId = definition.getVersionId();
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, definitionVersionId);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, definitionVersionId);
    }

    @Override
    protected void prepare(FileForm fileForm) {
        definitionVersionId = fileForm.getId();
    }
}
