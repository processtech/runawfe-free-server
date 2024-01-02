package ru.runa.wf.web.tag;

import java.util.List;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.CssClassStrategy;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ActivateProcessesAction;
import ru.runa.wf.web.action.CancelProcessesAction;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

@Tag(bodyContent = BodyContent.JSP, name = "viewProcessErrors")
public class ShowProcessErrorsTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfTokenError> errors = Delegates.getSystemService().getTokenErrors(getUser(), batchPresentation);
        int errorsCount = Delegates.getSystemService().getTokenErrorsCount(getUser(), batchPresentation);

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, errorsCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);

        TdBuilder[] builders = BatchPresentationUtils.getBuilders(
                new TdBuilder[]{new CheckboxTdBuilder("processId", Permission.CANCEL_PROCESS)}, batchPresentation, null);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);
        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(errors, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_MANAGE_PROCESS, getReturnAction(), "processId", builders);

        rowBuilder.setCssClassStrategy(new CssClassStrategy() {
            @Override
            public String getClassName(Object item, User user) {
                return ((WfTokenError)item).getProcessExecutionStatus() == ExecutionStatus.SUSPENDED ? Resources.CLASS_SUSPENDED : "";
            }

            @Override
            public String getCssStyle(Object item) {
                return null;
            }
        });
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder, false));
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(getActivateProcessesButton());
    }

    private Input getActivateProcessesButton() {
        Input button = new Input(Input.BUTTON, SUBMIT_BUTTON_NAME, MessagesProcesses.BUTTON_ACTIVATE_PROCESSES.message(pageContext));
        button.setClass(Resources.CLASS_BUTTON);
        button.setStyle("float: right;");
        String actionUrl = Commons.getActionUrl(ActivateProcessesAction.ACTION_PATH, getSubmitButtonParam(), pageContext, PortletUrlType.Action);
        button.setOnClick("activateProcesses('" + actionUrl + "')");
        return button;
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_CANCEL_PROCESSES.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return true;
    }

    @Override
    public String getAction() {
        return CancelProcessesAction.ACTION_PATH;
    }
}
