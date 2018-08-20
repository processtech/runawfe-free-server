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

import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.service.client.DelegateTaskVariableProvider;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.VariableProvider;

public class TaskDescriptionTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD(getValue(object, env));
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTask task = (WfTask) object;
        String description = task.getDescription();
        if (description != null && description.contains("${")) {
            VariableProvider variableProvider = new DelegateTaskVariableProvider(env.getUser(), task);
            FormHashModel model = new FormHashModel(env.getUser(), variableProvider, new StrutsWebHelper(env.getPageContext()));
            description = FreemarkerProcessor.process(description, model);
        }
        return description;
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
