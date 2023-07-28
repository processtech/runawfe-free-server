package ru.runa.wf.web.html;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.wfe.task.dto.WfTask;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class AssignTaskCheckboxTdBuilder extends CheckboxTdBuilder {

    boolean enableControl = true;

    public AssignTaskCheckboxTdBuilder() {
        super(null, null);
    }

    public AssignTaskCheckboxTdBuilder(boolean enableControl) {
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
