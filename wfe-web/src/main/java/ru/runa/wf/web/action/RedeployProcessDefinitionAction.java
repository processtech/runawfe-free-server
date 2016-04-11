/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf.web.action;

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

import com.google.common.base.Strings;

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

    private Long definitionId;

    @Override
    protected void doAction(User user, FileForm fileForm, List<String> processType, boolean isUpdateCurrentVersion) throws IOException {
        WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(user, fileForm.getId());
        byte[] data = Strings.isNullOrEmpty(fileForm.getFile().getFileName()) ? null : fileForm.getFile().getFileData();
        if (isUpdateCurrentVersion) {
            definition = Delegates.getDefinitionService().updateProcessDefinition(user, fileForm.getId(), data);
        } else {
            definition = Delegates.getDefinitionService().redeployProcessDefinition(user, fileForm.getId(), data, processType);
        }
        definitionId = definition.getId();
    }

    @Override
    protected ActionForward getSuccessAction(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_SUCCESS), IdForm.ID_INPUT_NAME, definitionId);
    }

    @Override
    protected ActionForward getErrorForward(ActionMapping mapping) {
        return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), IdForm.ID_INPUT_NAME, definitionId);
    }

    @Override
    protected void prepare(FileForm fileForm) {
        definitionId = fileForm.getId();
    }
}
