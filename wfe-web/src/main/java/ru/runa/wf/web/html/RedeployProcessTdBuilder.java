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
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.Permission;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class RedeployProcessTdBuilder extends BaseTdBuilder {

    public RedeployProcessTdBuilder() {
        super(Permission.UPDATE);
    }

    @Override
    public TD build(Object object, Env env) {
        WfDefinition pd = (WfDefinition) object;
        ConcreteElement startLink;

        if (isEnabled(object, env)) {
            startLink = new A(Commons.getActionUrl(WebResources.ACTION_MAPPING_REDEPLOY_PROCESS_DEFINITION, IdForm.ID_INPUT_NAME, pd.getVersionId(),
                    env.getPageContext(), PortletUrlType.Action), MessagesProcesses.LABEL_REDEPLOY_PROCESS_DEFINIION.message(env.getPageContext()));
        } else {
            startLink = new StringElement();
        }
        TD td = new TD(startLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return MessagesProcesses.LABEL_REDEPLOY_PROCESS_DEFINIION.message(env.getPageContext());
    }
}
