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

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.form.RelationPairForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

/**
 * List relations which contain executor on the right side.
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listExecutorRightRelationsForm")
public class ListExecutorRightRelationsFormTag extends SecuredObjectFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<Executor> executors = new ArrayList<>();
        executors.add(getSecuredObject());
        BatchPresentation executorBatchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        executors.addAll(Delegates.getExecutorService().getExecutorGroups(getUser(), getSecuredObject(), executorBatchPresentation, false));
        List<Relation> relations = Delegates.getRelationService().getRelationsContainingExecutorsOnRight(getUser(), executors);
        TableBuilder tableBuilder = new TableBuilder();
        TDBuilder[] builders = BatchPresentationUtils.getBuilders(null, BatchPresentationFactory.RELATIONS.createDefault(), null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(Lists.newArrayList(relations), executorBatchPresentation, pageContext,
                WebResources.ACTION_MAPPING_MANAGE_RELATION, "", new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(getNames());
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_EXECUTOR_RIGHT_RELATIONS.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected Executor getSecuredObject() {
        return Delegates.getExecutorService().getExecutor(getUser(), getIdentifiableId());
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
    }

    protected String[] getNames() {
        BatchPresentation batchPresentation = BatchPresentationFactory.RELATIONS.createDefault();
        FieldDescriptor[] fields = batchPresentation.getDisplayFields();
        String[] result = new String[fields.length];
        for (int i = 0; i < fields.length; ++i) {
            result[i] = Messages.getMessage(fields[i].displayName, pageContext);
        }
        return result;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            Map<String, Object> params = new HashMap<>();
            params.put(RelationPairForm.RELATION_ID, ((Relation) item).getId());
            params.put(RelationPairForm.EXECUTOR_TO, getIdentifiableId());
            return Commons.getActionUrl(baseUrl, params, pageContext, PortletUrlType.Action);
        }
    }
}
