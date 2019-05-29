package ru.runa.common.web.html;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.security.Permission;

public class CheckboxTdBuilder extends BaseTdBuilder {
    private final String actionFormId;
    private final String objectReadProperty;

    public CheckboxTdBuilder(String objectReadProperty, Permission permission) {
        this(objectReadProperty, permission, IdsForm.IDS_INPUT_NAME);
    }

    public CheckboxTdBuilder(String objectReadProperty, Permission permission, String actionFormId) {
        super(permission);
        this.actionFormId = actionFormId;
        this.objectReadProperty = objectReadProperty;
    }

    @Override
    public TD build(Object object, Env env) {
        Input input = new Input(Input.CHECKBOX, actionFormId, getIdValue(object));

        if (!isEnabled(object, env)) {
            input.setDisabled(true);
        }
        TD td = new TD(input);
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return "";
    }

    protected String getIdValue(Object object) {
        return readProperty(object, objectReadProperty, true);
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
