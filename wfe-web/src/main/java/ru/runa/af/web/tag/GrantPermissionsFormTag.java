package ru.runa.af.web.tag;

import java.util.List;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.GrantPermissionsAction;
import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.AllEnabledSecuredObjectCheckboxTdBuilder;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag2;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "grantPermissionsForm")
public class GrantPermissionsFormTag extends SecuredObjectFormTag2 {

    private static final long serialVersionUID = 1L;

    private String batchPresentationId;

    private String returnAction;

    @Override
    public void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.UPDATE_PERMISSIONS, getSecuredObject());
        super.fillFormElement(tdFormElement);
        if (returnAction != null) {
            tdFormElement.addElement(new Input(Input.HIDDEN, "returnAction", returnAction));
        }

        AuthorizationService authorizationService = Delegates.getAuthorizationService();
        BatchPresentation batchPresentation = getProfile().getActiveBatchPresentation(batchPresentationId);
        List<Executor> executors = authorizationService.getExecutorsWithPermission(getUser(), getSecuredObject(), batchPresentation, false);
        int executorsCount = authorizationService.getExecutorsWithPermissionCount(getUser(), getSecuredObject(), batchPresentation, false);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, executorsCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder[] prefixBuilders = new TdBuilder[] { new AllEnabledSecuredObjectCheckboxTdBuilder() };
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(prefixBuilders, batchPresentation, null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(executors, batchPresentation, pageContext, WebResources.ACTION_MAPPING_UPDATE_EXECUTOR,
                getReturnAction(), IdForm.ID_INPUT_NAME, builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

    @Attribute(required = true)
    public void setBatchPresentationId(String batchPresentationId) {
        this.batchPresentationId = batchPresentationId;
    }

    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    @Override
    public String getAction() {
        return GrantPermissionsAction.ACTION_PATH;
    }

    public String getReturnAction() {
        return returnAction;
    }

    @Attribute
    public void setReturnAction(String returnAction) {
        this.returnAction = returnAction;
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_GRANT_PERMISSION.message(pageContext);
    }
}
