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

import java.io.Serializable;
import java.util.Map;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TDBuilder.Env.IdentifiableExtractor;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.form.TaskIdForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.collect.Maps;

/**
 * Created on 14.11.2005
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
public class TaskProcessIdTDBuilder implements TDBuilder, Serializable {
    private static final long serialVersionUID = 1L;

    public TaskProcessIdTDBuilder() {
    }

    @Override
    public TD build(Object object, Env env) {
        WfTask task = (WfTask) object;
        Long processId = task.getProcessId();
        ConcreteElement link = new StringElement(processId.toString());
        boolean isAllowed = false;
        try {
            isAllowed = env.isAllowed(Permission.READ, new IdentifiableExtractor() {
                private static final long serialVersionUID = 1L;

                @Override
                public Identifiable getIdentifiable(final Object o, final Env env) {
                    Process identifiable = new Process();
                    identifiable.setId(((WfTask) o).getProcessId());
                    return identifiable;
                }

            });
        } catch (Exception e) {
        }
        if (isAllowed) {
            Map<String, Object> params = Maps.newHashMap();
            if (task.getProcessId() == null) {
                params.put(IdForm.ID_INPUT_NAME, processId);
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, task.getId());
            } else {
                params.put(IdForm.ID_INPUT_NAME, task.getProcessId());
                params.put(TaskIdForm.TASK_ID_INPUT_NAME, task.getId());
            }
            String url = Commons.getActionUrl(ShowGraphModeHelper.getManageProcessAction(), params, env.getPageContext(), PortletUrlType.Render);
            link = new A(url, link);
        }
        TD td = new TD(link);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTask task = (WfTask) object;
        return task.getProcessId().toString();
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
