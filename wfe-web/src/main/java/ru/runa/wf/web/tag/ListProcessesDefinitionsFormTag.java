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
import ru.runa.common.web.html.CheckboxTDBuilder;
import ru.runa.common.web.html.EnvBaseImpl;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UndeployProcessDefinitionsAction;
import ru.runa.wf.web.html.PropertiesProcessTDBuilder;
import ru.runa.wf.web.html.StartProcessTDBuilder;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
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
        TDBuilder[] builders = BatchPresentationUtils.getBuilders(
                new TDBuilder[] { new CheckboxTDBuilder("id", Permission.ALL), new StartProcessTDBuilder() },
                batchPresentation, new TDBuilder[] { new PropertiesProcessTDBuilder() });
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
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.DEFINITIONS);
        // TODO If (when) hidden types & permissions are implemented, uncomment and review/edit this.
//        for (boolean undeploy : Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UNDEPLOY_DEFINITION, definitions)) {
//            if (undeploy) {
//                return true;
//            }
//        }
//        return false;
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
