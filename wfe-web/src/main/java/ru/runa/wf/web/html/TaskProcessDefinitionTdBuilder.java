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

import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 09.03.2006
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class TaskProcessDefinitionTdBuilder implements TdBuilder {

    public TaskProcessDefinitionTdBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        WfTask task = (WfTask) object;
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        Long definitionId = getDefinitionId(task);
        String definitionName = getValue(object, env);
        if (env.hasProcessDefinitionPermission(Permission.READ, definitionId)) {
            td.addElement(new A(Commons.getActionUrl(WebResources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME,
                    definitionId, env.getPageContext(), PortletUrlType.Render), definitionName));
        } else {
            td.addElement(new StringElement(definitionName));
        }
        return td;
    }

    protected Long getDefinitionId(WfTask task) {
        return task.getDefinitionId();
    }

    @Override
    public String getValue(Object object, Env env) {
        return ((WfTask) object).getDefinitionName();
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
