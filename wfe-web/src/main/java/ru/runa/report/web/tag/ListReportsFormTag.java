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
package ru.runa.report.web.tag;

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
import ru.runa.common.web.html.CheckboxTDBuilder;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.report.web.action.UndeployReportAction;
import ru.runa.report.web.html.ReportPropertiesTDBuilder;
import ru.runa.wf.web.html.IdentifiableUrlStrategy;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.report.ReportsSecure;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.security.Permission;
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
        TDBuilder[] builders =
                BatchPresentationUtils.getBuilders(new TDBuilder[] { new CheckboxTDBuilder("id", Permission.DEPLOY_REPORT) }, batchPresentation,
                    new TDBuilder[] { new ReportPropertiesTDBuilder() });
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
        List<String> prefixCellsHeaders = new ArrayList<String>();
        int grouppingCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, reports, new EnvImpl(batchPresentation));
        for (int i = 0; i < 1 + grouppingCells; ++i) {
            prefixCellsHeaders.add("");
        }
        return prefixCellsHeaders.toArray(new String[prefixCellsHeaders.size()]);
    }

    private boolean isUndeployAllowed(List<WfReport> reports) {
        boolean hasGlobalDeployPermission = Delegates.getAuthorizationService().isAllowed(getUser(), Permission.DEPLOY_REPORT, ReportsSecure.INSTANCE);
        for (boolean undeploy : Delegates.getAuthorizationService().isAllowed(getUser(), Permission.DEPLOY_REPORT, reports)) {
            if (undeploy || hasGlobalDeployPermission) {
                return true;
            }
        }
        return false;
    }

    class EnvImpl extends EnvBaseImpl {

        BatchPresentation batchPresentation = null;

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
            return new IdentifiableUrlStrategy(pageContext).getUrl(WebResources.ACTION_MAPPING_BUILD_REPORT, object);
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) {
            return false;
        }
    }

    @Override
    public String getFormButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() {
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
