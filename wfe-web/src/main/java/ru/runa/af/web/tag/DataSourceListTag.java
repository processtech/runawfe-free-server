package ru.runa.af.web.tag;

import javax.servlet.http.HttpServletRequest;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.DeleteDataSourceAction;
import ru.runa.af.web.html.DataSourceTableBuilder;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesDataSource;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "dataSourceList")
public class DataSourceListTag extends TitledFormTag {

    private static final long serialVersionUID = -4263750161023575386L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        if (!Delegates.getExecutorService().isAdministrator(getUser())) {
            throw new AuthorizationException("No permission on this page");
        }
        getForm().setName("dataSourceList");
        getForm().setID("dataSourceList");
        getForm().setAction(
                ((HttpServletRequest) pageContext.getRequest()).getContextPath() + DeleteDataSourceAction.DELETE_DATA_SOURCE_ACTION_PATH + ".do");
        getForm().setMethod("post");
        tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, "1"));
        tdFormElement.addElement(new DataSourceTableBuilder(pageContext).build());
    }

    @Override
    protected String getTitle() {
        return MessagesDataSource.TITLE_DATA_SOURCES.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    public String getAction() {
        return DeleteDataSourceAction.DELETE_DATA_SOURCE_ACTION_PATH;
    }

    @Override
    public boolean isSubmitButtonEnabled() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_DATA_SOURCE_PARAMETER;
    }
}
