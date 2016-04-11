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
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.CreateRelationPairAction;
import ru.runa.af.web.form.RelationPairForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;

public class CreateRelationPairFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    private Long relationId;

    @Override
    public String getAction() {
        return CreateRelationPairAction.ACTION_PATH;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_CREATE_RELATION_PAIR, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_CREATE, pageContext);
    }

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        ActorSelect actorSelectFrom = new ActorSelect(getUser(), RelationPairForm.EXECUTOR_FROM, null, false);
        table.addElement(HTMLUtils.createSelectRow(Messages.getMessage(Messages.LABEL_RELATION_FROM, pageContext), actorSelectFrom, true));
        ActorSelect actorSelectTo = new ActorSelect(getUser(), RelationPairForm.EXECUTOR_TO, null, false);
        table.addElement(HTMLUtils.createSelectRow(Messages.getMessage(Messages.LABEL_RELATION_TO, pageContext), actorSelectTo, true));
        tdFormElement.addElement(table);
        tdFormElement.addElement(new Input(Input.HIDDEN, RelationPairForm.RELATION_ID, String.valueOf(relationId)));
    }
}
