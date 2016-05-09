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

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTDBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.StartDisabledImageProcessAction;
import ru.runa.wf.web.action.StartImageProcessAction;
import ru.runa.wf.web.tag.DefinitionUrlStrategy;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author Gordienko_m
 * @author Vitaliy S
 */
public class StartProcessTDBuilder extends BaseTDBuilder {

    public StartProcessTDBuilder() {
        super(DefinitionPermission.START_PROCESS);
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition definition = (WfDefinition) object;
        ConcreteElement startLink;

        String href;
        if (definition.isCanBeStarted()) {
            if (definition.hasStartImage()) {
                href = Commons.getActionUrl(StartImageProcessAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(), env.getPageContext(),
                    PortletUrlType.Resource);
            } else {
                href = Commons.getUrl(WebResources.START_PROCESS_IMAGE, env.getPageContext(), PortletUrlType.Resource);
            }
        } else {
            if (definition.hasDisabledImage()) {
                href = Commons.getActionUrl(StartDisabledImageProcessAction.ACTION_PATH, IdForm.ID_INPUT_NAME, definition.getId(),
                    env.getPageContext(), PortletUrlType.Resource);
            } else {
                href = Commons.getUrl(WebResources.START_PROCESS_DISABLED_IMAGE, env.getPageContext(), PortletUrlType.Resource);
            }
        }
        IMG startImg = new IMG(href);
        String startMessage = MessagesProcesses.LABEL_START_PROCESS.message(env.getPageContext());
        startImg.setAlt(startMessage);
        startImg.setBorder(0);
        if (definition.isCanBeStarted()) {
            String url = new DefinitionUrlStrategy(env.getPageContext()).getUrl(WebResources.ACTION_MAPPING_START_PROCESS, definition);
            startLink = new A(url).addElement(startImg);
            if (ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_PARAMETER)
                    || ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER)) {
                DefinitionService definitionService = Delegates.getDefinitionService();
                String actionParameter = null;
                if (!(definitionService.getStartInteraction(env.getUser(), definition.getId()).hasForm() || definitionService
                        .getOutputTransitionNames(env.getUser(), definition.getId(), null, false).size() > 1)) {
                    actionParameter = ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER;
                    startLink.addAttribute("onclick",
                        ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(actionParameter, env.getPageContext()));
                }
            }
        } else {
            startLink = new StringElement().addElement(startImg);
        }
        TD td = new TD(startLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfDefinition pd = (WfDefinition) object;
        return String.valueOf(pd.getId());
    }
}
