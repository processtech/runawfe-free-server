package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.GroupState;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UndeployProcessDefinitionsAction;
import ru.runa.wf.web.html.PropertiesProcessTdBuilder;
import ru.runa.wf.web.html.StartProcessTdBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 30.09.2004
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listProcessesDefinitionsForm")
public class ListProcessesDefinitionsFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 8409543832272909874L;

    private boolean isButtonEnabled;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        DefinitionService definitionService = Delegates.getDefinitionService();
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfDefinition> definitions = definitionService.getProcessDefinitions(getUser(), batchPresentation, false);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, definitions.size());
        navigation.addPagingNavigationTable(tdFormElement);
        isButtonEnabled = isUndeployAllowed(definitions);
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(
                new TdBuilder[] { new CheckboxTdBuilder("id", Permission.DELETE), new StartProcessTdBuilder() },
                batchPresentation,
                new TdBuilder[] { new PropertiesProcessTdBuilder() }
        );
        String[] prefixCellsHeaders = getGrouppingCells(batchPresentation, definitions);
        SortingHeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, prefixCellsHeaders, new String[] { "" }, getReturnAction(),
                pageContext);
        RowBuilder rowBuilder = new ReflectionRowBuilder(definitions, batchPresentation, pageContext, WebResources.ACTION_MAPPING_START_PROCESS,
                getReturnAction(), new DefinitionUrlStrategy(pageContext), builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    private String[] getGrouppingCells(BatchPresentation batchPresentation, List<WfDefinition> definitions) {
        List<String> prefixCellsHeaders = new ArrayList<>();
        int grouppingCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, definitions, new EnvImpl(batchPresentation));
        for (int i = 0; i < 1 + grouppingCells; ++i) {
            prefixCellsHeaders.add("");
        }
        prefixCellsHeaders.add(MessagesProcesses.LABEL_START_PROCESS.message(pageContext));
        return prefixCellsHeaders.toArray(new String[prefixCellsHeaders.size()]);
    }

    private boolean isUndeployAllowed(List<WfDefinition> definitions) {
        for (boolean undeploy : Delegates.getAuthorizationService().isAllowed(getUser(), Permission.DELETE, definitions)) {
            if (undeploy) {
                return true;
            }
        }
        return false;
    }

    class EnvImpl extends EnvBaseImpl {

        public EnvImpl(BatchPresentation batch) {
            batchPresentation = batch;
        }

        @Override
        public PageContext getPageContext() {
            return pageContext;
        }

        @Override
        public BatchPresentation getBatchPresentation() {
            return batchPresentation;
        }

        @Override
        public String getURL(Object object) {
            return new DefinitionUrlStrategy(pageContext).getUrl(WebResources.ACTION_MAPPING_START_PROCESS, object);
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, SecuredObjectExtractor extractor) {
            return false;
        }

        BatchPresentation batchPresentation;
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_UNDEPLOY_DEFINITION.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return isButtonEnabled;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITIONS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UndeployProcessDefinitionsAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.UNDEPLOY_PROCESS_DEFINITION_PARAMETER;
    }
}
