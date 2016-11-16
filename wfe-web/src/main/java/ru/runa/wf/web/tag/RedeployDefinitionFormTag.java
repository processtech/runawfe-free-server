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

import java.text.MessageFormat;

import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.ecs.html.A;
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
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.DefinitionCategoriesIterator;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.LockProcessDefinitionAction;
import ru.runa.wf.web.action.LockProcessDefinitionForAllAction;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.action.UnLockProcessDefinitionAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "redeployDefinitionForm")
public class RedeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    public static final String TYPE_UPDATE_CURRENT_VERSION = "updateCurrentVersion";

    private static final long serialVersionUID = 5106903896165128752L;

    public static void fillTD(TD tdFormElement, Form form, String[] definitionTypes, User user, PageContext pageContext) {
        form.setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        Input fileInput = HTMLUtils.createInput(Input.FILE, FileForm.FILE_INPUT_NAME, "", true, true);
        table.addElement(HTMLUtils.createRow(MessagesProcesses.LABEL_DEFINITIONS_ARCHIVE.message(pageContext), fileInput));
        DefinitionCategoriesIterator iterator = new DefinitionCategoriesIterator(user);
        TD hierarchyType = CategoriesSelectUtils.createSelectTD(iterator, definitionTypes, pageContext);
        table.addElement(HTMLUtils.createRow(Messages.getMessage(DefinitionClassPresentation.TYPE, pageContext), hierarchyType));
        tdFormElement.addElement(table);
        table.addElement(HTMLUtils.createCheckboxRow(MessagesProcesses.LABEL_UPDATE_CURRENT_VERSION.message(pageContext), TYPE_UPDATE_CURRENT_VERSION,
                false, true, false));
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        fillTD(tdFormElement, getForm(), getDefinition().getCategories(), getUser(), pageContext);
        fillTDLock(tdFormElement);
    }

    private void fillTDLock(TD tdFormElement) {
        final Table table = new Table();
        final TR tr = new TR();

        final WfDefinition definition = getDefinition();
        final String lockUserName = definition.getLockUserName();
        final Long definitionId = definition.getId();
        if (StringUtils.isEmpty(lockUserName)) {
            tr.addElement(createLockElement(definitionId));
            tr.addElement(createLockAllElement(definitionId));
        } else {
            if (WfDefinition.ALL_USERS.equals(lockUserName)) {
                final TD tdLockedForAll = new TD();
                tdLockedForAll.addElement(MessageFormat.format(MessagesOther.LABEL_LOCKED_FOR_ALL.message(pageContext), definition.getLockDate()));
                tr.addElement(tdLockedForAll);
            } else if (getUser().getName().equals(lockUserName)) {
                tr.addElement(createLockAllElement(definitionId));
            } else {
                final TD tdLocked = new TD();
                tdLocked.addElement(
                        MessageFormat.format(MessagesOther.LABEL_LOCKED_USER.message(pageContext), lockUserName, definition.getLockDate()));
                tr.addElement(tdLocked);
            }
            tr.addElement(createUnLockElement(definitionId));
        }
        table.addElement(tr);
        tdFormElement.addElement(table);
    }

    private TD createLockElement(final Long definitionId) {
        final TD tdLock = new TD();
        final String lockUrl = Commons.getActionUrl(LockProcessDefinitionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definitionId, pageContext,
                PortletUrlType.Render);
        final A lock = new A(lockUrl, MessagesOther.LABEL_LOCK.message(pageContext));
        lock.setClass(Resources.CLASS_LINK);
        tdLock.addElement(lock);
        return tdLock;
    }

    private TD createLockAllElement(final Long definitionId) {
        final TD tdLockAll = new TD();
        final String lockAllUrl = Commons.getActionUrl(LockProcessDefinitionForAllAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definitionId, pageContext,
                PortletUrlType.Render);
        final A lockAll = new A(lockAllUrl, MessagesOther.LABEL_LOCK_FOR_ALL.message(pageContext));
        lockAll.setClass(Resources.CLASS_LINK);
        tdLockAll.addElement(lockAll);
        return tdLockAll;
    }

    private TD createUnLockElement(final Long definitionId) {
        final TD tdUnLock = new TD();
        final String unLockUrl = Commons.getActionUrl(UnLockProcessDefinitionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definitionId, pageContext,
                PortletUrlType.Render);
        final A unLock = new A(unLockUrl, MessagesOther.LABEL_UNLOCK.message(pageContext));
        unLock.setClass(Resources.CLASS_LINK);
        tdUnLock.addElement(unLock);
        return tdUnLock;
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

    @Override
    protected boolean isFormButtonEnabled() {
        boolean enabled = super.isFormButtonEnabled();
        if (enabled) {
            final WfDefinition definition = getDefinition();
            final String lockUserName = definition.getLockUserName();
            if (!StringUtils.isEmpty(lockUserName) && !getUser().getName().equals(lockUserName)) {
                enabled = false;
            }
        }
        return enabled;
    }
}
