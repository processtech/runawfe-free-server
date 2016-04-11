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

import org.apache.ecs.html.TD;

import ru.runa.af.web.action.RemoveRelationAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.IdentifiableCheckboxTDBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPermission;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.service.delegate.Delegates;

public class ListRelationsFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;
    private boolean formButtonVisible;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        formButtonVisible = Delegates.getAuthorizationService().isAllowed(getUser(), RelationPermission.UPDATE, RelationsGroupSecure.INSTANCE);
        List<Relation> relations = Delegates.getRelationService().getRelations(getUser(), getBatchPresentation());
        TableBuilder tableBuilder = new TableBuilder();
        TDBuilder checkboxBuilder = new IdentifiableCheckboxTDBuilder(RelationPermission.UPDATE) {

            @Override
            protected boolean isEnabled(Object object, Env env) {
                return formButtonVisible;
            }
        };
        TDBuilder[] builders = getBuilders(new TDBuilder[] { checkboxBuilder }, getBatchPresentation(), new TDBuilder[] {});
        RowBuilder rowBuilder = new ReflectionRowBuilder(relations, getBatchPresentation(), pageContext, WebResources.ACTION_MAPPING_MANAGE_RELATION,
                getReturnAction(), new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(getBatchPresentation(), 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_RELATIONS, pageContext);
    }

    @Override
    protected boolean isFormButtonEnabled() {
        return formButtonVisible;
    }

    @Override
    protected boolean isFormButtonVisible() {
        return formButtonVisible;
    }

    @Override
    protected boolean isMultipleSubmit() {
        return false;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    public String getAction() {
        return RemoveRelationAction.ACTION_PATH;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            return Commons.getActionUrl(baseUrl, RelationForm.RELATION_ID, ((Relation) item).getId(), pageContext, PortletUrlType.Action);
        }

    }
}
