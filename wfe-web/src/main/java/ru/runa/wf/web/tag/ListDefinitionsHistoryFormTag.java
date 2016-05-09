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

import java.util.List;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.PropertiesProcessTDBuilder;
import ru.runa.wf.web.html.UndeployProcessDefinitionTDBuilder;
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
        TDBuilder[] builders = getBuilders(new TDBuilder[] {}, batchPresentation, new TDBuilder[] { new UndeployProcessDefinitionTDBuilder(),
                new PropertiesProcessTDBuilder() });
        SortingHeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 0, 2, getReturnAction(), pageContext);
        RowBuilder rowBuilder = new ReflectionRowBuilder(definitions, batchPresentation, pageContext, WebResources.ACTION_MAPPING_MANAGE_DEFINITION,
                getReturnAction(), new DefinitionUrlStrategy(pageContext), builders);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return false;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITIONS.message(pageContext);
    }
}
