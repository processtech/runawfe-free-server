package ru.runa.report.web.tag;

import com.google.common.primitives.Booleans;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.GroupState;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.report.web.action.UndeployReportAction;
import ru.runa.report.web.html.ReportPropertiesTdBuilder;
import ru.runa.wf.web.html.SecuredObjectUrlStrategy;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.ReportService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listReportsForm")
public class ListReportsFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = 8409543832272909874L;

    private boolean isButtonEnabled;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        ReportService reportService = Delegates.getReportService();
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfReport> reports = reportService.getReportDefinitions(getUser(), batchPresentation, false);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, reports.size());
        navigation.addPagingNavigationTable(tdFormElement);
        isButtonEnabled = isUndeployAllowed(reports);
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(
                new TdBuilder[] { new CheckboxTdBuilder("id", Permission.UPDATE) },
                batchPresentation,
                new TdBuilder[] { new ReportPropertiesTdBuilder() });
        String[] prefixCellsHeaders = getGrouppingCells(batchPresentation, reports);
        SortingHeaderBuilder headerBuilder =
                new SortingHeaderBuilder(batchPresentation, prefixCellsHeaders, new String[] { "" }, getReturnAction(), pageContext);
        RowBuilder rowBuilder =
                new ReflectionRowBuilder(reports, batchPresentation, pageContext, WebResources.ACTION_MAPPING_BUILD_REPORT, getReturnAction(), "id",
                        builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    private String[] getGrouppingCells(BatchPresentation batchPresentation, List<WfReport> reports) {
        List<String> prefixCellsHeaders = new ArrayList<>();
        int grouppingCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, reports, new EnvImpl(batchPresentation));
        for (int i = 0; i < 1 + grouppingCells; ++i) {
            prefixCellsHeaders.add("");
        }
        return prefixCellsHeaders.toArray(new String[prefixCellsHeaders.size()]);
    }

    private boolean isUndeployAllowed(List<WfReport> reports) {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredSingleton.REPORTS) ||
                Booleans.contains(Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, reports), true);
    }

    class EnvImpl extends EnvBaseImpl {

        BatchPresentation batchPresentation;

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
            return new SecuredObjectUrlStrategy(pageContext).getUrl(WebResources.ACTION_MAPPING_BUILD_REPORT, object);
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, SecuredObjectExtractor extractor) {
            return false;
        }
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return isButtonEnabled;
    }

    @Override
    protected String getTitle() {
        return MessagesCommon.MAIN_MENU_ITEM_REPORTS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UndeployReportAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.UNDEPLOY_REPORT_PARAMETER;
    }
}
