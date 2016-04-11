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
package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.web.action.CreateExecutorAction;
import ru.runa.af.web.form.CreateExecutorForm;
import ru.runa.af.web.html.ExecutorTableBuilder;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;

/**
 * Created on 18.08.2004
 * 
 * @jsp.tag name = "createExecutorForm" body-content = "empty"
 */
public class CreateExecutorFormTag extends TitledFormTag {
    private static final long serialVersionUID = 8049519129092850184L;
    private String type;

    @Override
    protected String getTitle() {
        String key;
        if (CreateExecutorForm.TYPE_ACTOR.equals(type)) {
            key = "title.create_actor";
        } else {
            key = "title.create_group";
        }
        return Messages.getMessage(key, pageContext);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void fillFormElement(TD tdFormElement) {
        boolean isActor = CreateExecutorForm.TYPE_ACTOR.equals(type);
        ExecutorTableBuilder builder = new ExecutorTableBuilder(isActor, pageContext);
        tdFormElement.addElement(builder.buildTable());
        tdFormElement.addElement(createHiddenType());
    }

    private Input createHiddenType() {
        return new Input(Input.HIDDEN, CreateExecutorForm.EXECUTOR_TYPE_INPUT_NAME, type);
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    @Override
    public String getAction() {
        return CreateExecutorAction.ACTION_PATH;
    }
}
