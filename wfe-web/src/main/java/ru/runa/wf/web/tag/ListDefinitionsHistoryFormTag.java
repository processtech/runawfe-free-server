package ru.runa.wf.web.tag;

import java.util.List;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.PropertiesProcessTdBuilder;
import ru.runa.wf.web.html.UndeployProcessDefinitionTdBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listDefinitionsHistoryForm")
public class ListDefinitionsHistoryFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 2203850190079109329L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        DefinitionService definitionService = Delegates.getDefinitionService();
        BatchPresentation batchPresentation = getBatchPresentation();
        int count = definitionService.getProcessDefinitionsCount(getUser(), batchPresentation);
        List<WfDefinition> definitions = definitionService.getDeployments(getUser(), batchPresentation, true);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, count, "/definitions_history.do");
        navigation.addPagingNavigationTable(tdFormElement);
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, new TdBuilder[] {
                new UndeployProcessDefinitionTdBuilder(), new PropertiesProcessTdBuilder() });
        SortingHeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 0, 2, getReturnAction(), pageContext);
        RowBuilder rowBuilder = new ReflectionRowBuilder(definitions, batchPresentation, pageContext, WebResources.ACTION_MAPPING_MANAGE_DEFINITION,
                getReturnAction(), new DefinitionUrlStrategy(pageContext), builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return false;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITIONS.message(pageContext);
    }
}
