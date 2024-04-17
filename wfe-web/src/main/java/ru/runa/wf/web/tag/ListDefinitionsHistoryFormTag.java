package ru.runa.wf.web.tag;

import com.google.common.base.Charsets;
import java.net.URLEncoder;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.RadioButtonTdBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ShowDefinitionHistoryDiffAction;
import ru.runa.wf.web.html.PropertiesProcessTdBuilder;
import ru.runa.wf.web.html.UndeployProcessDefinitionTdBuilder;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionHistoryClassPresentation;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listDefinitionsHistoryForm")
public class ListDefinitionsHistoryFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 2203850190079109329L;
    public static final String ACTION_PATH = "/definitions_history";
    public static final String NAME_PARAMETER = "name";

    @SneakyThrows
    @Override
    protected void fillFormElement(TD tdFormElement) {
        String definitionName = pageContext.getRequest().getParameter(NAME_PARAMETER);
        if (definitionName == null) {
            throw new InternalApplicationException("No name parameter has been provided");
        }
        String pageNumberString = pageContext.getRequest().getParameter(PagingNavigationHelper.PAGE_PARAMETER);
        if (pageNumberString == null) {
            throw new InternalApplicationException("No page parameter has been provided");
        }
        BatchPresentation batchPresentation = getBatchPresentation();
        batchPresentation.setPageNumber(Integer.parseInt(pageNumberString));
        int nameFieldIndex = batchPresentation.getType().getFieldIndex(DefinitionHistoryClassPresentation.NAME);
        batchPresentation.getFilteredFields().put(nameFieldIndex, new StringFilterCriteria(definitionName));
        DefinitionService definitionService = Delegates.getDefinitionService();
        int count = definitionService.getProcessDefinitionsCount(getUser(), batchPresentation);
        List<WfDefinition> definitions = definitionService.getDeployments(getUser(), batchPresentation, true);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, count, ACTION_PATH + ".do?" + NAME_PARAMETER
                + "=" + URLEncoder.encode(definitionName, Charsets.UTF_8.name()) + "&" + PagingNavigationHelper.PAGE_PARAMETER_GAP);
        navigation.addPagingNavigationTable(tdFormElement);
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(
                new TdBuilder[] { new RadioButtonTdBuilder(ShowDefinitionHistoryDiffAction.VERSION_1, "version"),
                        new RadioButtonTdBuilder(ShowDefinitionHistoryDiffAction.VERSION_2, "version") },
                batchPresentation, new TdBuilder[] { new UndeployProcessDefinitionTdBuilder(), new PropertiesProcessTdBuilder() });
        SortingHeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 2, 2, getReturnAction(), pageContext, false);
        RowBuilder rowBuilder = new ReflectionRowBuilder(definitions, batchPresentation, pageContext, WebResources.ACTION_MAPPING_MANAGE_DEFINITION,
                getReturnAction(), new DefinitionUrlStrategy(pageContext), builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, ShowDefinitionHistoryDiffAction.DEFINITION_NAME, definitionName));
        tdFormElement.addElement(new Input(Input.HIDDEN, ShowDefinitionHistoryDiffAction.CONTEXT_LINES_COUNT,
                WebResources.getProcessDefinitionDiffContextLinesCount()));
        getForm().setTarget("_blank");
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return true;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return true;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION_HISTORY.message(pageContext);
    }

    @Override
    public String getAction() {
        return ShowDefinitionHistoryDiffAction.ACTION;
    }

    @Override
    public String getMethod() {
        return Form.GET;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_VIEW_DIFFERENCES.message(pageContext);
    }

}
