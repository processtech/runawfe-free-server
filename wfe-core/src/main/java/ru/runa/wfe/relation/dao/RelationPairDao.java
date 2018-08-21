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

import com.google.common.collect.Lists;
import com.querydsl.jpa.JPQLQuery;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.relation.QRelationPair;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.user.Executor;

/**
 * Relation pair dao implementation.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 * @since 3.3
 */
@Component
public class RelationPairDao extends GenericDao<RelationPair> {

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
        sessionFactory.getCurrentSession().save(result);
        return result;
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param relation
     *            {@link Relation} can be null.
     * @param from
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
     * @param from
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorsRelationPairsLeft(Relation relation, Collection<? extends Executor> from) {
        return getRelationPairs(relation, from, null);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     */
    public List<RelationPair> getRelationPairs(Relation relation, BatchPresentation batchPresentation) {
        Map<Integer, FilterCriteria> filters = batchPresentation.getFilteredFields();
        try {
            filters.put(0, new StringFilterCriteria(relation.getName()));
            return new PresentationCompiler<RelationPair>(batchPresentation).getBatch(CompilerParameters.createNonPaged());
        } finally {
            filters.remove(0);
        }
    }

    /**
     * Deleted all relation pairs for executor.
     */
    public void removeAllRelationPairs(Executor executor) {
        val rp = QRelationPair.relationPair;
        queryFactory.delete(rp).where(rp.left.eq(executor).or(rp.right.eq(executor))).execute();
    }

    private List<RelationPair> getRelationPairs(Relation relation, Collection<? extends Executor> left, Collection<? extends Executor> right) {
        val rp = QRelationPair.relationPair;
        JPQLQuery<RelationPair> q = queryFactory.selectFrom(rp);
        if (relation != null) {
            q.where(rp.relation.eq(relation));
        }
        if (left != null) {
            q.where(rp.left.in(left));
        }
        if (right != null) {
            q.where(rp.right.in(right));
        }
        return q.fetch();
    }
}
