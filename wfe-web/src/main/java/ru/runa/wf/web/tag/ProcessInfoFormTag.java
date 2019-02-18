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

import com.google.common.collect.Maps;
import java.util.Map;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Element;
import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ActivateProcessExecutionAction;
import ru.runa.wf.web.action.CancelProcessAction;
import ru.runa.wf.web.action.RemoveProcessAction;
import ru.runa.wf.web.action.RestoreProcessAction;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.action.SuspendProcessExecutionAction;
import ru.runa.wf.web.action.UpgradeProcessToDefinitionVersionAction;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processInfoForm")
public class ProcessInfoFormTag extends ProcessBaseFormTag {
    private static final long serialVersionUID = -1275657878697999574L;

    private Long taskId;

    @Attribute
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTaskId() {
        return taskId;
    }

    // start #179

    @Override
    protected Permission getSubmitPermission() {
        // @see #isSubmitButtonEnabled()
        return null;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return isSubmitButtonEnabled(getSecuredObject(), null);
    }

    @Override
    protected boolean isSubmitButtonEnabled(SecuredObject securedObject, Permission permission) {
        boolean ended = getProcess().isEnded();
        if (ended) {
            return WebResources.isProcessRemovalEnabled() && Delegates.getExecutorService().isAdministrator(getUser());
        } else {
            return super.isSubmitButtonEnabled(securedObject, Permission.CANCEL);
        }
    }

    @Override
    public String getAction() {
        boolean ended = getProcess().isEnded();
        return ended ? RemoveProcessAction.ACTION_PATH : CancelProcessAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        boolean ended = getProcess().isEnded();
        return ended ? ConfirmationPopupHelper.REMOVE_PROCESS_PARAMETER : ConfirmationPopupHelper.CANCEL_PROCESS_PARAMETER;
    }

    @Override
    public String getSubmitButtonName() {
        boolean ended = getProcess().isEnded();
        return ended ? MessagesCommon.BUTTON_REMOVE.message(pageContext) : MessagesProcesses.BUTTON_CANCEL_PROCESS.message(pageContext);
    }

    // end #179

    @Override
    protected void fillFormData(TD tdFormElement) {
        WfProcess process = getProcess();
        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);

        TR nameTR = new TR();
        table.addElement(nameTR);
        String definitionName = Messages.getMessage(DefinitionClassPresentation.NAME, pageContext);
        nameTR.addElement(new TD(definitionName).setClass(Resources.CLASS_LIST_TABLE_TD));

        Element processDefinitionHref;
        try {
            WfDefinition definition = Delegates.getDefinitionService().getProcessDefinition(getUser(), process.getDefinitionId());
            String url = Commons.getActionUrl(ru.runa.common.WebResources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME, definition.getId(),
                    pageContext, PortletUrlType.Render);
            processDefinitionHref = new A(url, process.getName());
        } catch (Exception e) {
            processDefinitionHref = new StringElement(process.getName());
        }
        nameTR.addElement(new TD(processDefinitionHref).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR processIdTR = new TR();
        table.addElement(processIdTR);
        String idName = Messages.getMessage(ProcessClassPresentation.PROCESS_ID, pageContext);
        processIdTR.addElement(new TD(idName).setClass(Resources.CLASS_LIST_TABLE_TD));
        processIdTR.addElement(new TD(String.valueOf(process.getId())).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR versionTR = new TR();
        table.addElement(versionTR);
        String definitionVersion = Messages.getMessage(DefinitionClassPresentation.VERSION, pageContext);
        versionTR.addElement(new TD(definitionVersion).setClass(Resources.CLASS_LIST_TABLE_TD));
        Element versionElement = new StringElement(String.valueOf(process.getVersion()));
        versionElement = addUpgradeLinkIfRequired(process, versionElement);
        versionTR.addElement(new TD(versionElement).setClass(Resources.CLASS_LIST_TABLE_TD));

        TR startedTR = new TR();
        table.addElement(startedTR);
        String startedName = Messages.getMessage(ProcessClassPresentation.PROCESS_START_DATE, pageContext);
        startedTR.addElement(new TD(startedName).setClass(Resources.CLASS_LIST_TABLE_TD));
        startedTR.addElement(new TD(CalendarUtil.formatDateTime(process.getStartDate())).setClass(Resources.CLASS_LIST_TABLE_TD));

        if (process.getExecutionStatus() != null) {
            ConcreteElement statusElement = new Span(Messages.getMessage(process.getExecutionStatus().getLabelKey(), pageContext));
            switch (process.getExecutionStatus()) {
            case ACTIVE:
                if (SystemProperties.isProcessSuspensionEnabled() && Delegates.getExecutorService().isAdministrator(getUser())) {
                    Div div = new Div();
                    div.addElement(statusElement);
                    div.addElement(Entities.NBSP);
                    div.addElement(new A(Commons.getActionUrl(SuspendProcessExecutionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, process.getId(),
                            pageContext, PortletUrlType.Render), MessagesProcesses.PROCESS_SUSPEND.message(pageContext)));
                    statusElement = div;
                }
                break;
            case FAILED:
            case SUSPENDED:
                statusElement.setClass(Resources.CLASS_SUSPENDED);
                if (Delegates.getExecutorService().isAdministrator(getUser())) {
                    Div div = new Div();
                    div.addElement(statusElement);
                    div.addElement(Entities.NBSP);
                    StrutsMessage message = process.getExecutionStatus() == ExecutionStatus.FAILED ? MessagesProcesses.PROCESS_ACTIVATE_FAILED_TOKENS
                            : MessagesProcesses.PROCESS_ACTIVATE;
                    div.addElement(new A(Commons.getActionUrl(ActivateProcessExecutionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, process.getId(),
                            pageContext, PortletUrlType.Render), message.message(pageContext)));
                    statusElement = div;
                }
                break;
            case ENDED:
                Div div = new Div();
                div.addElement(statusElement);
                div.addElement(Entities.NBSP);
                div.addElement(CalendarUtil.formatDateTime(process.getEndDate()));

                A restoreLink = new A();
                Map<String, String> parameters = Maps.newHashMap();
                parameters.put(IdForm.ID_INPUT_NAME, process.getId().toString());
                restoreLink.setHref(Commons.getActionUrl(RestoreProcessAction.ACTION_PATH, parameters, pageContext, PortletUrlType.Render));
                restoreLink.setClass(Resources.CLASS_BUTTON);
                restoreLink.setStyle("margin-left: 5px");
                restoreLink.addElement(MessagesCommon.BUTTON_RESTORE.message(pageContext));
                div.addElement(restoreLink);

                statusElement = div;
                break;
            default:
                throw new InternalApplicationException(String.valueOf(process.getExecutionStatus()));
            }
            TR statusTR = new TR();
            String statusLabel = Messages.getMessage(ProcessClassPresentation.PROCESS_EXECUTION_STATUS, pageContext);
            statusTR.addElement(new TD(statusLabel).setClass(Resources.CLASS_LIST_TABLE_TD));
            statusTR.addElement(new TD(statusElement).setClass(Resources.CLASS_LIST_TABLE_TD));
            table.addElement(statusTR);
        }

        WfProcess parentProcess = Delegates.getExecutionService().getParentProcess(getUser(), getIdentifiableId());
        if (parentProcess != null) {
            TR parentTR = new TR();
            table.addElement(parentTR);
            String parentNameString = MessagesProcesses.LABEL_PARENT_PROCESS.message(pageContext);
            parentTR.addElement(new TD(parentNameString).setClass(Resources.CLASS_LIST_TABLE_TD));
            TD td = new TD();
            td.setClass(Resources.CLASS_LIST_TABLE_TD);
            Element inner;
            String parentProcessDefinitionName = parentProcess.getName();
            if (checkReadable(parentProcess)) {
                Map<String, Object> params = Maps.newHashMap();
                params.put(IdForm.ID_INPUT_NAME, parentProcess.getId());
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, taskId);
                params.put("childProcessId", process.getId());
                inner = new A(Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, pageContext, PortletUrlType.Render),
                        parentProcessDefinitionName);
            } else {
                inner = new StringElement(parentProcessDefinitionName);
            }
            td.addElement(inner);
            parentTR.addElement(td);
        }
    }

    private Element addUpgradeLinkIfRequired(WfProcess process, Element versionElement) {
        if (!SystemProperties.isUpgradeProcessToDefinitionVersionEnabled()) {
            return versionElement;
        }
        Div div = new Div();
        div.addElement(versionElement);
        div.addElement(Entities.NBSP);
        String url = Commons.getActionUrl(UpgradeProcessToDefinitionVersionAction.ACTION_PATH, IdForm.ID_INPUT_NAME, process.getId(), pageContext,
                PortletUrlType.Render);
        A upgradeLink = new A(url, MessagesProcesses.PROCESS_UPGRADE_TO_DEFINITION_VERSION.message(pageContext));
        upgradeLink.addAttribute("data-definitionName", process.getName());
        upgradeLink.addAttribute("data-definitionVersion", process.getVersion());
        upgradeLink.setOnClick("selectProcessUpgrageVersionDialog(this); return false;");
        div.addElement(upgradeLink);
        return div;
    }

    private boolean checkReadable(WfProcess parentProcess) {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.LIST, parentProcess);
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS.message(pageContext);
    }

}
