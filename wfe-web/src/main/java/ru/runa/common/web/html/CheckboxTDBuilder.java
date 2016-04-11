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
package ru.runa.common.web.html;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.security.Permission;

public class CheckboxTDBuilder extends BaseTDBuilder {
    private final String actionFormId;
    private final String objectReadProperty;

    public CheckboxTDBuilder(String objectReadProperty, Permission permission) {
        this(objectReadProperty, permission, IdsForm.IDS_INPUT_NAME);
    }

    public CheckboxTDBuilder(String objectReadProperty, Permission permission, String actionFormId) {
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
