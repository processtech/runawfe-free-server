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

public class RadioButtonTdBuilder extends BaseTdBuilder {
    private final String inputName;
    private final String propertyName;

    public RadioButtonTdBuilder(String inputName, String propertyName) {
        super(null);
        this.inputName = inputName;
        this.propertyName = propertyName;
    }

    @Override
    public TD build(Object object, Env env) {
        Input input = new Input(Input.RADIO, inputName, getValue(object, env));
        TD td = new TD(input);
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return readProperty(object, propertyName, true);
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
