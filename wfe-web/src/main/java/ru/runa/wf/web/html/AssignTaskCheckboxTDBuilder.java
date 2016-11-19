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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.common.web.html.CheckboxTDBuilder;
import ru.runa.wfe.task.dto.WfTask;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class AssignTaskCheckboxTDBuilder extends CheckboxTDBuilder {

    boolean enableControl = true;

    public AssignTaskCheckboxTDBuilder() {
        super(null, null);
    }

    public AssignTaskCheckboxTDBuilder(boolean enableControl) {
        super(null, null);
        this.enableControl = enableControl;
    }

    @Override
    public TD build(Object object, Env env) {
        Input input = new Input(Input.CHECKBOX, StrIdsForm.IDS_INPUT_NAME, getIdValue(object));

        if (!isEnabled(object, env)) {
            input.setDisabled(true);
        }
        if (isChecked(object, env)) {
            input.setChecked(true);
        }

        TD td = new TD(input);

        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    protected boolean isEnabled(Object object, Env env) {
        WfTask task = (WfTask) object;
        return task.isGroupAssigned() && enableControl;
    }

    protected boolean isChecked(Object object, Env env) {
        WfTask task = (WfTask) object;
        return !task.isGroupAssigned();
    }

    @Override
    public String getValue(Object object, Env env) {
        return "";
    }

    @Override
    protected String getIdValue(Object object) {
        WfTask task = (WfTask) object;
        Long ownerId = null;
        if (task.getOwner() != null) {
            ownerId = task.getOwner().getId();
        }
        return task.getId() + ":" + String.valueOf(ownerId);
    }
}
