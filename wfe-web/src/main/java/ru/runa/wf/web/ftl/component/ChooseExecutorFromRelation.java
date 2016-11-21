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
package ru.runa.wf.web.ftl.component;

import java.util.List;
import java.util.Set;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ChooseExecutorFromRelation extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        String relationName = getRichComboParameterAs(String.class, 1);
        boolean inversed = getParameterAs(boolean.class, 2);
        BatchPresentation batchPresentation = BatchPresentationFactory.RELATION_PAIRS.createNonPaged();
        List<RelationPair> pairs = Delegates.getRelationService().getRelationPairs(user, relationName, batchPresentation);
        Set<Executor> executors = Sets.newHashSet();
        for (RelationPair pair : pairs) {
            Executor executor = inversed ? pair.getRight() : pair.getLeft();
            try {
                Delegates.getExecutorService().getExecutor(user, executor.getId());
                executors.add(executor);
            } catch (AuthorizationException e) {
                // TODO may be filter executors in logic?
                // http://sourceforge.net/tracker/?func=detail&aid=3478716&group_id=125156&atid=701698
            }
        }
        return ViewUtil.createExecutorSelect(variableName, Lists.newArrayList(executors), variableProvider.getValue(variableName), true, true);
    }

}
