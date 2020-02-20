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
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.CreateRelationAction;
import ru.runa.af.web.action.UpdateRelationAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "relationForm")
public class RelationFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    private Long relationId;
    private boolean enabled = true;

    public Long getRelationId() {
        return relationId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    @Override
    public String getAction() {
        return relationId != null ? UpdateRelationAction.ACTION_PATH : CreateRelationAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return (relationId != null ? MessagesCommon.BUTTON_SAVE : MessagesCommon.BUTTON_CREATE).message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_RELATION_DETAILS.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        if (relationId != null) {
            enabled = Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredObjectType.RELATION, relationId);
        }
        return enabled;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Relation relation = null;
        if (relationId != null) {
            relation = Delegates.getRelationService().getRelation(getUser(), relationId);
        }
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        if (relation != null) {
            Input hiddenRelationID = new Input(Input.HIDDEN, RelationForm.RELATION_ID, String.valueOf(relationId));
            tdFormElement.addElement(hiddenRelationID);
        }
        String name = relation != null ? relation.getName() : "";
        Input nameInput = HTMLUtils.createInput(RelationForm.RELATION_NAME, name, enabled, true);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.LABEL_RELATION_NAME.message(pageContext), nameInput));
        String description = relation != null ? relation.getDescription() : "";
        Input descriptionInput = HTMLUtils.createInput(RelationForm.RELATION_DESCRIPTION, description);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.LABEL_RELATION_DESCRIPTION.message(pageContext), descriptionInput));
        tdFormElement.addElement(table);
    }
}
