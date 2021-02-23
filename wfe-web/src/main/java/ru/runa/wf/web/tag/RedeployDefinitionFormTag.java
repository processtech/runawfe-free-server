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

import javax.servlet.jsp.PageContext;
import org.apache.ecs.Element;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.DefinitionCategoriesIterator;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.action.UpgradeProcessesToDefinitionVersionAction;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "redeployDefinitionForm")
public class RedeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    public static final String TYPE_UPDATE_CURRENT_VERSION = "updateCurrentVersion";

    private static final long serialVersionUID = 5106903896165128752L;
    private static RedeployDefinitionFormTag instance;

    protected void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext) {
        form.setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        Input fileInput = HTMLUtils.createInput(Input.FILE, FileForm.FILE_INPUT_NAME, "", true, true, "." + FileDataProvider.PAR_FILE);
        table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_DEFINITIONS_ARCHIVE.message(pageContext), fileInput));
        DefinitionCategoriesIterator iterator = new DefinitionCategoriesIterator(user);
        TD hierarchyType = CategoriesSelectUtils.createSelectTD(iterator, definitionTypes, pageContext);
        table.addElement(HTMLUtils.createRow(Messages.getMessage(DefinitionClassPresentation.TYPE, pageContext), hierarchyType));
        tdFormElement.addElement(table);
        table.addElement(HTMLUtils.createCheckboxRow(MessagesProcesses.LABEL_UPDATE_CURRENT_VERSION.message(pageContext),
                TYPE_UPDATE_CURRENT_VERSION, false, true, false));

        if (SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            WfDefinition wfDefinition = Delegates.getDefinitionService().getProcessDefinition(user, getIdentifiableId());

            TR upgradeProcessesTR = new TR();
            table.addElement(upgradeProcessesTR);
            Element upgradeProcessesElement = addUpgradeProcessesLink(wfDefinition);
            upgradeProcessesTR.addElement(new TD(upgradeProcessesElement).setColSpan(2).setClass(Resources.CLASS_LIST_TABLE_TD));
        }
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_REDEPLOY_DEFINITION.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
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
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, getDefinition());
    }

    private Element addUpgradeProcessesLink(WfDefinition definition) {
        Div div = new Div();
        String url = Commons.getActionUrl(UpgradeProcessesToDefinitionVersionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(),
                pageContext, PortletUrlType.Render);
        A upgradeProcessLink = new A(url, MessagesProcesses.PROCESSES_UPGRADE_TO_DEFINITION_VERSION.message(pageContext));
        upgradeProcessLink.addAttribute("data-definitionName", definition.getName());
        upgradeProcessLink.addAttribute("data-definitionVersion", definition.getVersion());
        upgradeProcessLink.setOnClick("selectProcessUpgrageVersionDialog(this); return false;");
        div.addElement(upgradeProcessLink);
        return div;
    }

    public static RedeployDefinitionFormTag getInstance() {
        if (instance == null) {
            instance = new RedeployDefinitionFormTag();
        }
        return instance;
    }
}
