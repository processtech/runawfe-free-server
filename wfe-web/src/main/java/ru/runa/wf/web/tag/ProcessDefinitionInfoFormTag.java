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

import java.util.Map;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdNameForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.LoadProcessDefinitionArchiveAction;
import ru.runa.wf.web.action.LockProcessDefinitionAction;
import ru.runa.wf.web.action.LockProcessDefinitionForAllAction;
import ru.runa.wf.web.action.ShowDefinitionHistoryAction;
import ru.runa.wf.web.action.UnlockProcessDefinitionAction;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskClassPresentation;

import com.google.common.collect.Maps;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processDefinitionInfoForm")
public class ProcessDefinitionInfoFormTag extends ProcessDefinitionBaseFormTag {
    private static final long serialVersionUID = 7118850164438509260L;

    @Override
    protected boolean isVisible() {
        return true;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        WfDefinition definition = getDefinition();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(TaskClassPresentation.DEFINITION_NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD nameTD = new TD();
        nameTD.setClass(Resources.CLASS_LIST_TABLE_TD);
        if (Delegates.getAuthorizationService().isAllowed(getUser(), WorkflowSystemPermission.DEPLOY_DEFINITION, ASystem.INSTANCE)) {
            nameTD.addElement(definition.getName() + " (");
            String historyUrl = Commons.getActionUrl(ShowDefinitionHistoryAction.ACTION, "name", definition.getName(), pageContext,
                    PortletUrlType.Render);
            nameTD.addElement(new A(historyUrl, MessagesProcesses.TITLE_DEFINITIONS_HISTORY.message(pageContext)));
            nameTD.addElement(")");
        } else {
            nameTD.addElement(definition.getName());
        }
        nameTR.addElement(nameTD);

        TR versionTR = new TR();
        table.addElement(versionTR);
        String versionName = Messages.getMessage(DefinitionClassPresentation.VERSION, pageContext);
        versionTR.addElement(new TD(versionName).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD versionTD = new TD();
        versionTD.addElement(definition.getVersion() + " (");
        String downloadUrl = Commons.getActionUrl(LoadProcessDefinitionArchiveAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(),
                pageContext, PortletUrlType.Render);
        versionTD.addElement(new A(downloadUrl, MessagesOther.LABEL_EXPORT.message(pageContext)));
        versionTD.addElement(")");
        versionTR.addElement(versionTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        TR createdDateTR = new TR();
        table.addElement(createdDateTR);
        String createDateMessage = Messages.getMessage(DefinitionClassPresentation.CREATE_DATE, pageContext);
        createdDateTR.addElement(new TD(createDateMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
        createdDateTR.addElement(new TD(CalendarUtil.formatDateTime(definition.getCreateDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR createdByTR = new TR();
        table.addElement(createdByTR);
        String createdByMessage = Messages.getMessage(DefinitionClassPresentation.CREATE_ACTOR, pageContext);
        createdByTR.addElement(new TD(createdByMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
        String createdBy = definition.getCreateActor() != null ? definition.getCreateActor().getLabel() : "";
        createdByTR.addElement(new TD(createdBy).setClass(Resources.CLASS_LIST_TABLE_TD));

        if (definition.getUpdateDate() != null) {
            TR updateDateTR = new TR();
            table.addElement(updateDateTR);
            String updateDateMessage = Messages.getMessage(DefinitionClassPresentation.UPDATE_DATE, pageContext);
            updateDateTR.addElement(new TD(updateDateMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
            updateDateTR.addElement(new TD(CalendarUtil.formatDateTime(definition.getUpdateDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

            TR updatedByTR = new TR();
            table.addElement(updatedByTR);
            String updatedByMessage = Messages.getMessage(DefinitionClassPresentation.UPDATE_ACTOR, pageContext);
            updatedByTR.addElement(new TD(updatedByMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
            String updatedBy = definition.getUpdateActor() != null ? definition.getUpdateActor().getLabel() : "";
            updatedByTR.addElement(new TD(updatedBy).setClass(Resources.CLASS_LIST_TABLE_TD));
        }

        if (definition.getLockDate() != null) {
            {
                TR lockDateTR = new TR();
                table.addElement(lockDateTR);
                String lockDateMessage = Messages.getMessage(DefinitionClassPresentation.LOCK_DATE, pageContext);
                lockDateTR.addElement(new TD(lockDateMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
                String message = CalendarUtil.formatDateTime(definition.getLockDate());
                if (definition.isLockForAll()) {
                    message += " (" + MessagesOther.LABEL_FOR_ALL.message(pageContext) + ")";
                }
                Div div = new Div();
                div.addElement(new Span(message));
                div.addElement(createUnlockLink(definition));
                lockDateTR.addElement(new TD(div).setClass(Resources.CLASS_LIST_TABLE_TD));
            }
            {
                TR lockByTR = new TR();
                table.addElement(lockByTR);
                String updatedByMessage = Messages.getMessage(DefinitionClassPresentation.LOCK_ACTOR, pageContext);
                lockByTR.addElement(new TD(updatedByMessage).setClass(Resources.CLASS_LIST_TABLE_TD));
                Div div = new Div();
                div.addElement(new Span(definition.getLockActor().getLabel()));
                if (!definition.isLockForAll()) {
                    div.addElement(createLockForAllLink(definition));
                }
                lockByTR.addElement(new TD(div).setClass(Resources.CLASS_LIST_TABLE_TD));
            }
        } else {
            TR lockTR = new TR();
            table.addElement(lockTR);
            lockTR.addElement(new TD(MessagesOther.LABEL_LOCKING.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            Div div = new Div();
            div.addElement(createLockLink(definition));
            div.addElement(createLockForAllLink(definition));
            lockTR.addElement(new TD(div).setClass(Resources.CLASS_LIST_TABLE_TD));
        }

        TR descriptionTR = new TR();
        table.addElement(descriptionTR);
        String description = Messages.getMessage(DefinitionClassPresentation.DESCRIPTION, pageContext);
        descriptionTR.addElement(new TD(description).setClass(Resources.CLASS_LIST_TABLE_TD));
        descriptionTR.addElement(new TD(definition.getDescription()).setClass(Resources.CLASS_LIST_TABLE_TD));
    }

    @Override
    protected Permission getPermission() {
        return DefinitionPermission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION.message(pageContext);
    }

    private A createLockLink(WfDefinition definition) {
        return createLink(LockProcessDefinitionAction.ACTION_PATH, definition, MessagesOther.LABEL_LOCK.message(pageContext), false);
    }

    private A createLockForAllLink(WfDefinition definition) {
        String message = MessagesOther.LABEL_LOCK.message(pageContext) + " " + MessagesOther.LABEL_FOR_ALL.message(pageContext);
        return createLink(LockProcessDefinitionForAllAction.ACTION_PATH, definition, message, true);
    }

    private A createUnlockLink(WfDefinition definition) {
        return createLink(UnlockProcessDefinitionAction.ACTION_PATH, definition, MessagesOther.LABEL_UNLOCK.message(pageContext), true);
    }

    private A createLink(String path, WfDefinition definition, String message, boolean margin) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put(IdNameForm.ID_INPUT_NAME, definition.getId());
        parameters.put(IdNameForm.NAME_INPUT_NAME, definition.getName());
        final String url = Commons.getActionUrl(path, parameters, pageContext, PortletUrlType.Render);
        final A a = new A(url, message);
        a.setClass(Resources.CLASS_LINK);
        if (margin) {
            a.setStyle("margin-left: 50px;");
        }
        return a;
    }

}
