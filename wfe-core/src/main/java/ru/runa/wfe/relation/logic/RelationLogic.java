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
package ru.runa.wfe.relation.logic;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.CompilerParameters;
import ru.runa.wfe.presentation.hibernate.PresentationCompiler;
import ru.runa.wfe.relation.QRelation;
import ru.runa.wfe.relation.QRelationPair;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDao;
import ru.runa.wfe.relation.dao.RelationPairDao;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Relation logic.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
public class RelationLogic extends CommonLogic {
    @Autowired
    private RelationDao relationDao;
    @Autowired
    private RelationPairDao relationPairDao;

    /**
     * Add {@link RelationPair} to {@link Relation} with specified name.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationId
     *            Relation id.
     * @param left
     *            Left part of relation pair.
     * @param right
     *            Right part of relation pair.
     * @return Created relation pair.
     */
    public RelationPair addRelationPair(User user, Long relationId, Executor left, Executor right) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredObjectType.RELATION, relationId);
        Relation relation = relationDao.getNotNull(relationId);
        return relationPairDao.addRelationPair(relation, left, right);
    }

    /**
     * Create {@link Relation} with specified name and description or throws {@link RelationAlreadyExistException} if relation with such name is
     * already exists.
     * 
     * @param user
     *            user, which perform operation.
     * @return Created relation.
     */
    public Relation createRelation(User user, Relation relation) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredSingleton.RELATIONS);
        return relationDao.create(relation);
    }

    public Relation updateRelation(User user, Relation relation) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredSingleton.RELATIONS);
        return relationDao.update(relation);
    }

    /**
     * Return list of {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param user
     *            user, which perform operation.
     * @param batchPresentation
     *            Restrictions to get relations.
     * @return List of {@link Relation}.
     */
    @SuppressWarnings("unchecked")
    public List<Relation> getRelations(User user, BatchPresentation batchPresentation) {
        return new PresentationCompiler(batchPresentation).getBatch(CompilerParameters.create(false));
    }

    /**
     * Return {@link RelationPair}s for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for all {@link Relation} returned.
     * @param right
     *            Collection of {@link Executor}, which contains in right part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorRelationPairsRight(User user, String relationName, List<? extends Executor> right) {
        Relation relation = relationName != null ? relationDao.getNotNull(relationName) : null;
        return relationPairDao.getExecutorsRelationPairsRight(relation, right);
    }

    /**
     * Return {@link RelationPair}s for specified {@link Relation}, which left part contains executor from 'left' parameter.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationName
     *            {@link Relation} name. If null, when {@link RelationPair} for all {@link Relation} returned.
     * @param left
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     */
    public List<RelationPair> getExecutorRelationPairsLeft(User user, String relationName, List<? extends Executor> left) {
        Relation relation = relationName != null ? relationDao.getNotNull(relationName) : null;
        return relationPairDao.getExecutorsRelationPairsLeft(relation, left);
    }

    public List<Relation> getRelationsContainingExecutorsOnLeft(User user, List<Executor> executors) {
        return getRelationsContainingExecutors(user, executors, true);
    }

    public List<Relation> getRelationsContainingExecutorsOnRight(User user, List<Executor> executors) {
        return getRelationsContainingExecutors(user, executors, false);
    }

    private List<Relation> getRelationsContainingExecutors(User user, List<Executor> executors, boolean onLeft) {
        QRelation r = QRelation.relation;
        QRelationPair rp = QRelationPair.relationPair;
        List<Relation> relations = Lists.newArrayList();
        List<List<Executor>> parts = Lists.partition(executors, SystemProperties.getDatabaseParametersCount());
        for (List<Executor> part : parts) {
            relations.addAll(
                    queryFactory.selectDistinct(r).from(r, rp).where(rp.relation.eq(r).and(onLeft ? rp.left.in(part) : rp.right.in(part))).fetch());
        }
        return relations;
    }

    /**
     * Return {@link Relation} with specified name or throws {@link RelationDoesNotExistException} if relation with such name does not exist.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationName
     *            Relation name
     * @return Relation with specified name.
     */
    public Relation getRelation(User user, String relationName) {
        return relationDao.getNotNull(relationName);
    }

    /**
     * Return {@link Relation} with specified identity or throws {@link RelationDoesNotExistException} if relation with such identity does not exist.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationId
     *            Relation identity.
     * @return Relation with specified name.
     */
    public Relation getRelation(User user, Long relationId) {
        return relationDao.getNotNull(relationId);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationName
     *            Relation name.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     */
    public List<RelationPair> getRelations(User user, String relationName, BatchPresentation batchPresentation) {
        Relation relation = relationDao.getNotNull(relationName);
        return relationPairDao.getRelationPairs(relation, batchPresentation);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationId
     *            Relation identity.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     */
    public List<RelationPair> getRelations(User user, Long relationId, BatchPresentation batchPresentation) {
        Relation relation = relationDao.getNotNull(relationId);
        return relationPairDao.getRelationPairs(relation, batchPresentation);
    }

    /**
     * Removes {@link RelationPair} with specified identity.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationPairId
     *            {@link RelationPair} identity.
     */
    public void removeRelationPair(User user, Long relationPairId) {
        RelationPair relationPair = relationPairDao.getNotNull(relationPairId);
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredObjectType.RELATION, relationPair.getRelation().getId());
        relationPairDao.delete(relationPair);
    }

    /**
     * Remove {@link Relation} with specified identity.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationId
     *            Relation identity.
     */
    public void removeRelation(User user, Long relationId) {
        permissionDao.checkAllowed(user, Permission.UPDATE, SecuredSingleton.RELATIONS);
        relationDao.delete(relationId);
    }
}
