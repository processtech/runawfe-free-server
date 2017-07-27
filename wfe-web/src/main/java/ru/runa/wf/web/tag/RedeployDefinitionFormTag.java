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
package ru.runa.wf.web.tag;

import org.apache.ecs.Element;
import org.apache.ecs.html.*;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.*;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.DefinitionCategoriesIterator;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.action.UpgradeProcessesToDefinitionVersionAction;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import javax.servlet.jsp.PageContext;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "redeployDefinitionForm")
public class RedeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    public static final String TYPE_UPDATE_CURRENT_VERSION = "updateCurrentVersion";

    private static final long serialVersionUID = 5106903896165128752L;
    private static RedeployDefinitionFormTag instance;

    protected void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext) {
        form.setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        Input fileInput = HTMLUtils.createInput(Input.FILE, FileForm.FILE_INPUT_NAME, "", true, true);
        table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_DEFINITIONS_ARCHIVE.message(pageContext), fileInput));
        DefinitionCategoriesIterator iterator = new DefinitionCategoriesIterator(user);
        TD hierarchyType = CategoriesSelectUtils.createSelectTD(iterator, definitionTypes, pageContext);
        table.addElement(HTMLUtils.createRow(Messages.getMessage(DefinitionClassPresentation.TYPE, pageContext), hierarchyType));
        tdFormElement.addElement(table);
        table.addElement(HTMLUtils.createCheckboxRow(MessagesProcesses.LABEL_UPDATE_CURRENT_VERSION.message(pageContext),
                TYPE_UPDATE_CURRENT_VERSION, false, true, false));

        if (SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            WfProcess process = Delegates.getExecutionService().getProcess(user, getIdentifiableId());

            TR upgradeProcessesTR = new TR();
            table.addElement(upgradeProcessesTR);
            Element upgradeProcessesElement = addUpgradeProcessesLink(process);
            upgradeProcessesTR.addElement(new TD(upgradeProcessesElement).setClass(Resources.CLASS_LIST_TABLE_TD));
        }
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext);
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.REDEPLOY_DEFINITION;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_REDEPLOY_DEFINITION.message(pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return MessagesProcesses.TITLE_REDEPLOY_DEFINITION.message(pageContext);
    }

    @Override
    public String getAction() {
        return RedeployProcessDefinitionAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REDEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    @Override
    protected boolean isVisible() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), DefinitionPermission.REDEPLOY_DEFINITION, getIdentifiable());
    }

    private Element addUpgradeProcessesLink(WfProcess process) {
        Div div = new Div();
        String url = Commons.getActionUrl(UpgradeProcessesToDefinitionVersionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, process.getId(), pageContext,
                PortletUrlType.Render);
        A upgradeProcessLink = new A(url, MessagesProcesses.PROCESSES_UPGRADE_TO_DEFINITION_VERSION.message(pageContext));
        upgradeProcessLink.addAttribute("data-processId", process.getId());
        upgradeProcessLink.addAttribute("data-definitionName", process.getName());
        upgradeProcessLink.addAttribute("data-definitionVersion", process.getVersion());
        upgradeProcessLink.setOnClick("selectProcessUpgrageVersionDialog(this); return false;");
        div.addElement(upgradeProcessLink);
        return div;
    }

    public static RedeployDefinitionFormTag getInstance() {
        if( instance == null ) {
            instance = new RedeployDefinitionFormTag();
        }
        return instance;
    }
}
