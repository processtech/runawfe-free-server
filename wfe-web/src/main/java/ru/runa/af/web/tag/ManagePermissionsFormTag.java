package ru.runa.af.web.tag;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.wml.A;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.UpdatePermissionsAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag2;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "managePermissionsForm")
public class ManagePermissionsFormTag extends SecuredObjectFormTag2 {
    private static final long serialVersionUID = 1L;

    @Override
    public String getAction() {
        return UpdatePermissionsAction.ACTION_PATH;
    }

    @Override
    public final void fillFormElement(TD tdFormElement) {
        SecuredObject o = getSecuredObject();
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.READ_PERMISSIONS, o);
        super.fillFormElement(tdFormElement);

        // This is for UpdatePermissionsAction to return back to current page:
        tdFormElement.addElement(new Input(Input.HIDDEN, "returnAction", Commons.getSelfActionWithQueryString(pageContext)));

        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(o, getUser(), pageContext);
        if (o.getSecuredObjectType() == SecuredObjectType.DEFINITION) {
            tableBuilder.addAdditionalExecutor(
                    Delegates.getExecutorService().getExecutorByName(getUser(), SystemExecutors.PROCESS_STARTER_NAME),
                    Lists.newArrayList(Iterables.filter(ApplicablePermissions.listVisible(o), new Predicate<Permission>() {
                        @Override
                        public boolean apply(Permission p) {
                            return p != Permission.READ_PROCESS && p != Permission.CANCEL_PROCESS;
                        }
                    }))
            );
        }
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesCommon.TITLE_PERMISSION_OWNERS.message(pageContext) + securedObjectLabel(getSecuredObject());
    }

    private String securedObjectLabel(SecuredObject so) {
        final String linkClass = "permissionLink";
        String type = so.getSecuredObjectType().getName();
        String name = null;
        if (so instanceof ProcessDefinitionPack) {
            ProcessDefinitionPack deployment = (ProcessDefinitionPack) so;
            name = new A("manage_process_definition.do?id=" + deployment.getId()).addElement(deployment.getName()).setClass(linkClass).toString();
        } else if (so instanceof Process) {
            WfProcess process = Delegates.getExecutionService().getProcess(getUser(), identifiableId);
            A processName = (A) new A("manage_process_definition.do?id=" + process.getDefinitionId()).addElement(process.getName())
                    .setClass(linkClass);
            A processId = (A) new A("manage_process.do?id=" + identifiableId).addElement(identifiableId.toString()).setClass(linkClass);
            name = processName + ", Id: " + processId;
        } else if (so instanceof Relation) {
            name = ((Relation) so).getName();
        } else if (so instanceof Executor) {
            type = so.getClass().getSimpleName();
            name = new A("manage_executor.do?id=" + identifiableId).addElement(((Executor) so).getLabel()).setClass(linkClass).toString();
        } else if (so instanceof WfReport) {
            name = ((WfReport) so).getName();
        }
        return " (" + Messages.getMessage("secured_object_type." + type.toLowerCase(), pageContext) + (Strings.isNullOrEmpty(name) ? "" : ": " + name)
                + ")";
    }
}
