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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ecs.html.TD;

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
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

/**
 * List relations in which executor exists in left side.
 */
public class ListExecutorLeftRelationsFormTag extends IdentifiableFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<Executor> executors = new ArrayList<Executor>();
        executors.add(getIdentifiable());
        BatchPresentation batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        for (Group group : Delegates.getExecutorService().getExecutorGroups(getUser(), getIdentifiable(), batchPresentation, false)) {
            executors.add(group);
        }
        Set<Relation> relations = new HashSet<Relation>();
        for (RelationPair pair : Delegates.getRelationService().getExecutorsRelationPairsLeft(getUser(), null, executors)) {
            relations.add(pair.getRelation());
        }
        TableBuilder tableBuilder = new TableBuilder();
        TDBuilder[] builders = getBuilders(new TDBuilder[] {}, BatchPresentationFactory.RELATIONS.createDefault(), new TDBuilder[] {});
        RowBuilder rowBuilder = new ReflectionRowBuilder(Lists.newArrayList(relations), batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_MANAGE_RELATION, "", new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(getNames());
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_EXECUTOR_LEFT_RELATIONS, pageContext);
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected Executor getIdentifiable() {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getExecutor(getUser(), getIdentifiableId());
    }

    @Override
    protected Permission getPermission() {
        return Permission.READ;
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
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(RelationPairForm.RELATION_ID, ((Relation) item).getId());
            params.put(RelationPairForm.EXECUTOR_FROM, getIdentifiableId());
            return Commons.getActionUrl(baseUrl, params, pageContext, PortletUrlType.Action);
        }
    }
}
