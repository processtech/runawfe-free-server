package ru.runa.wf.web.tag;

import java.util.List;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.RemoveTokensAction;
import ru.runa.wfe.execution.dto.WfToken;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listTokensForm")
public class ListTokensFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;

    private Long processId;

    @Attribute
    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        ExecutionService executionService = Delegates.getExecutionService();
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfToken> tokens = executionService.getProcessTokens(getUser(), processId, false);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, tokens.size());
        navigation.addPagingNavigationTable(tdFormElement);
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { new CheckboxTdBuilder("id", Permission.DELETE) },
                batchPresentation, null);
        HeaderBuilder headerBuilder = makeHeaderBuilder(batchPresentation);
        RowBuilder rowBuilder = new ReflectionRowBuilder(tokens, batchPresentation, pageContext, null, getReturnAction(), "id", builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, processId.toString()));
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_TOKENS.message(pageContext);
    }

    @Override
    public String getAction() {
        return RemoveTokensAction.ACTION_PATH;
    }

    private HeaderBuilder makeHeaderBuilder(BatchPresentation batchPresentation) {
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        String[] headers = new String[fields.length + 1];
        for (int i = 0; i < fields.length; i++) {
            headers[i + 1] = Messages.getMessage(batchPresentation, fields[i], pageContext);
        }
        return new StringsHeaderBuilder(headers);
    }

}