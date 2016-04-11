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
package ru.runa.wfe.relation.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

/**
 * Relation pair dao implementation.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 * @since 3.3
 */
@SuppressWarnings("unchecked")
public class RelationPairDAO extends GenericDAO<RelationPair> {

    @Override
    protected void checkNotNull(RelationPair entity, Object identity) {
        if (entity == null) {
            throw new RelationPairDoesNotExistException(identity);
        }
    }

    public RelationPair addRelationPair(Relation relation, Executor left, Executor right) {
        List<RelationPair> exists = getRelationPairs(relation, Lists.newArrayList(left), Lists.newArrayList(right));
        if (exists.size() > 0) {
            return exists.get(0);
        }
        RelationPair result = new RelationPair(relation, left, right);
        getHibernateTemplate().save(result);
        return result;
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param relation
     *            {@link Relation} can be null.
     * @param right
     *            Collection of {@link Executor}, which contains in right part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorsRelationPairsRight(Relation relation, Collection<? extends Executor> from) {
        return getRelationPairs(relation, null, from);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left part contains executor from 'left' parameter.
     * 
     * @param relation
     *            {@link Relation} can be null
     * @param right
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorsRelationPairsLeft(Relation relation, Collection<? extends Executor> from) {
        return getRelationPairs(relation, from, null);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param relationName
     *            Relation name
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     * @return
     */
    public List<RelationPair> getRelationPairs(Relation relation, BatchPresentation batchPresentation) {
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFields();
        try {
            filters.put(0, new StringFilterCriteria(relation.getName()));
            List<RelationPair> result = new PresentationCompiler<RelationPair>(batchPresentation).getBatch(CompilerParameters.createNonPaged());
            return result;
        } finally {
            filters.remove(0);
        }
    }

    /**
     * Deleted all relation pairs for executor.
     * 
     * @param executor
     */
    public void removeAllRelationPairs(Executor executor) {
        getHibernateTemplate().deleteAll(getRelationPairs(null, Lists.newArrayList(executor), null));
        getHibernateTemplate().deleteAll(getRelationPairs(null, null, Lists.newArrayList(executor)));
    }

    private List<RelationPair> getRelationPairs(final Relation relation, final Collection<? extends Executor> left,
            final Collection<? extends Executor> right) {
        return getHibernateTemplate().execute(new HibernateCallback<List<RelationPair>>() {

            @Override
            public List<RelationPair> doInHibernate(Session session) {
                Criteria criteria = session.createCriteria(RelationPair.class);
                if (relation != null) {
                    criteria.add(Restrictions.eq("relation", relation));
                }
                if (left != null) {
                    criteria.add(Restrictions.in("left", left));
                }
                if (right != null) {
                    criteria.add(Restrictions.in("right", right));
                }
                return criteria.list();
            }
        });
    }

}
