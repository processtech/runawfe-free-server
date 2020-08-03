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

import java.util.List;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.action.RemoveRelationPairsAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listRelationPairsForm")
public class ListRelationPairsFormTag extends BatchReturningTitledFormTag {
    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowedForAny(getUser(), Permission.DELETE, SecuredObjectType.RELATION);
    }
    
    @Override
    protected boolean isSubmitButtonVisible() {
        return Delegates.getAuthorizationService().isAllowedForAny(getUser(), Permission.DELETE, SecuredObjectType.RELATION);
    }    

    private static final long serialVersionUID = 1L;
    private Long relationId;

    public Long getRelationId() {
        return relationId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.READ, SecuredObjectType.RELATION, relationId);
        Relation relation = Delegates.getRelationService().getRelation(getUser(), relationId);
        BatchPresentation batchPresentation = getBatchPresentation();
        List<RelationPair> relationPairs = Delegates.getRelationService().getRelationPairs(getUser(), relation.getName(), batchPresentation);
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder checkboxBuilder = new CheckboxTdBuilder(null, Permission.UPDATE) {

            @Override
            protected String getIdValue(Object object) {
                return String.valueOf(((RelationPair) object).getId());
            }

            @Override
            protected boolean isEnabled(Object object, Env env) {
                return true;
            }
        };
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { checkboxBuilder }, batchPresentation, null);
        for (TdBuilder td: builders) {
            if (td instanceof BaseTdBuilder) {
                ((BaseTdBuilder) td).setPermission(Permission.READ);
            }
        }        
        RowBuilder rowBuilder = new ReflectionRowBuilder(relationPairs, batchPresentation, pageContext, WebResources.ACTION_MAPPING_UPDATE_EXECUTOR,
                getReturnAction(), IdForm.ID_INPUT_NAME, builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        tdFormElement.addElement(new Input(Input.HIDDEN, RelationForm.RELATION_ID, relationId.toString()));
    }

    @Override
    protected String getTitle() {
        return Delegates.getRelationService().getRelation(getUser(), relationId).getName();
    }

    @Override
    public String getAction() {
        return RemoveRelationPairsAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }
}
