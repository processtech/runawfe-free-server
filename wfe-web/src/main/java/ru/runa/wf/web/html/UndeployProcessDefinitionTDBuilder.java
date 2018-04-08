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
package ru.runa.wf.web.html;

import java.util.Map;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import com.google.common.collect.Maps;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdVersionForm;
import ru.runa.common.web.html.BaseTDBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.UndeployProcessDefinitionAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.LongFilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @since 4.3.0
 */
public class UndeployProcessDefinitionTDBuilder extends BaseTDBuilder {

    public UndeployProcessDefinitionTDBuilder() {
        super(Permission.UNDEPLOY_DEFINITION);
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        BatchPresentation presentation = BatchPresentationFactory.PROCESSES.createDefault();
        int definitionNameFieldIndex = presentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.DEFINITION_NAME);
        int definitionVersionFieldIndex = presentation.getClassPresentation().getFieldIndex(ProcessClassPresentation.DEFINITION_VERSION);
        presentation.getFilteredFields().put(definitionNameFieldIndex, new StringFilterCriteria(definition.getName()));
        presentation.getFilteredFields().put(definitionVersionFieldIndex, new LongFilterCriteria(definition.getVersion()));
        int allCount = Delegates.getExecutionService().getProcessesCount(env.getUser(), presentation);
        ConcreteElement element;
        if (isEnabled(object, env) && allCount == 0) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put(IdForm.ID_INPUT_NAME, definition.getId());
            parameters.put(IdVersionForm.VERSION_INPUT_NAME, definition.getVersion());
            String url = Commons.getActionUrl(UndeployProcessDefinitionAction.ACTION_PATH, parameters, env.getPageContext(), PortletUrlType.Render);
            element = new A(url, MessagesProcesses.BUTTON_UNDEPLOY_DEFINITION.message(env.getPageContext()));
        } else {
            element = new StringElement();
        }
        TD td = new TD(element);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return MessagesProcesses.BUTTON_UNDEPLOY_DEFINITION.message(env.getPageContext());
    }
}
