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

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.*;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.*;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.LoadProcessDefinitionArchiveAction;
import ru.runa.wf.web.action.ShowDefinitionHistoryAction;
import ru.runa.wf.web.html.DefinitionChangesHeaderBuilder;
import ru.runa.wf.web.html.PropertiesProcessTDBuilder;
import ru.runa.wf.web.html.UndeployProcessDefinitionTDBuilder;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.WorkflowSystemPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.dto.WfProcessDefinitionChange;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskClassPresentation;

import java.util.List;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listProcessDefinitionChangesForm")
public class ListProcessDefinitionChangesFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 7128850164438509265L;

    private Long processDefinitionId;

    @Attribute(required = true, rtexprvalue = true)
    public void setProcessDefinitionId(Long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public Long getProcessDefinitionId() {
        return processDefinitionId;
    }

    @Override
    protected boolean isVisible() {
        return true;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        DefinitionService definitionService = Delegates.getDefinitionService();
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfProcessDefinitionChange> changes = definitionService.getChanges(processDefinitionId);
        TDBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, new TDBuilder[] { });
        DefinitionChangesHeaderBuilder headerBuilder = new DefinitionChangesHeaderBuilder(batchPresentation, pageContext);
        RowBuilder rowBuilder = new ReflectionRowBuilder(changes, batchPresentation, pageContext, WebResources.ACTION_MAPPING_MANAGE_DEFINITION,
                getReturnAction(), new DefinitionChangesUrlStrategy(pageContext), builders);

        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
        tdFormElement.setNoWrap(false);
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION_CHANGES.message(pageContext);
    }
}