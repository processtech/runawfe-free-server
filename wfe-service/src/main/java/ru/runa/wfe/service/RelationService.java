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
package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationPairDoesNotExistException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Relations service interface.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 * @since 3.1
 */
public interface RelationService {
    /**
     * Creates {@link Relation} with specified name and description or throws {@link RelationAlreadyExistException} if relation with such name is
     * already exists.
     * 
     * @param user
     *            User, which perform operation.
     * @param relation
     *            Relation to create.
     * @return Created relation.
     * @throws RelationAlreadyExistException
     *             Relation already exists.
     */
    Relation createRelation(User user, Relation relation) throws RelationAlreadyExistException;

    /**
     * Updates {@link Relation}.
     * 
     * @param user
     *            User, which perform operation.
     * @param relation
     *            Relation to update.
     * @return Updated relation.
     * @throws RelationDoesNotExistException
     *             Relation does not exist with such id.
     */
    Relation updateRelation(User user, Relation relation) throws RelationDoesNotExistException;

    /**
     * Return list of {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param user
     *            User, which perform operation.
     * @param batchPresentation
     *            Restrictions to get relations.
     * @return List of {@link Relation}.
     */
    List<Relation> getRelations(User user, BatchPresentation batchPresentation);

    /**
     * Return {@link Relation} with specified name or throws {@link RelationDoesNotExistException} if relation with such name does not exists.
     * 
     * @param user
     *            User, which perform operation.
     * @param name
     *            Relation name.
     * @return Relation with specified name.
     * @throws RelationDoesNotExistException
     *             Relation with specified name is not exists.
     */
    Relation getRelationByName(User user, String name) throws RelationDoesNotExistException;

    /**
     * Return {@link Relation} with specified identity or throws {@link RelationDoesNotExistException} if relation with such identity does not exists.
     * 
     * @param user
     *            User, which perform operation.
     * @param id
     *            Relation identity.
     * @return Relation with specified name.
     * @throws RelationDoesNotExistException
     *             Relation with specified name is not exists.
     */
    Relation getRelation(User user, Long id) throws RelationDoesNotExistException;

    /**
     * Remove {@link Relation} with specified identity.
     * 
     * @param user
     *            User, which perform operation.
     * @param id
     *            Relation identity.
     * @throws RelationDoesNotExistException
     *             Relation with specified identity does not exist.
     */
    void removeRelation(User user, Long id) throws RelationDoesNotExistException;

    /**
     * Add {@link RelationPair} to {@link Relation} with specified name.
     * 
     * @param user
     *            User, which perform operation.
     * @param relationId
     *            Relation id.
     * @param left
     *            Left part of relation pair.
     * @param right
     *            Right part of relation pair.
     * @return Created relation pair.
     * @throws RelationDoesNotExistException
     *             Relation with specified name does not exist.
     */
    RelationPair addRelationPair(User user, Long relationId, Executor left, Executor right) throws RelationDoesNotExistException;

    /**
     * Removes {@link RelationPair} with specified identity.
     * 
     * @param user
     *            User, which perform operation.
     * @param id
     *            {@link RelationPair} identity.
     * @throws RelationPairDoesnotExistException
     *             {@link RelationPair} does not exist.
     */
    void removeRelationPair(User user, Long id) throws RelationPairDoesNotExistException;

    /**
     * Removes {@link RelationPair}'s with specified identity.
     * 
     * @param user
     *            User, which perform operation.
     * @param ids
     *            {@link RelationPair} identity.
     * @throws RelationPairDoesnotExistException
     *             {@link RelationPair} does not exist.
     */
    void removeRelationPairs(User user, List<Long> ids) throws RelationPairDoesNotExistException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, according to specified {@link BatchPresentation}.
     * 
     * @param user
     *            User, which perform operation.
     * @param name
     *            Relation name.
     * @param batchPresentation
     *            Restrictions to get {@link RelationPair}.
     */
    List<RelationPair> getRelationPairs(User user, String name, BatchPresentation batchPresentation) throws RelationDoesNotExistException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
     * 
     * @param user
     *            User, which perform operation.
     * @param name
     *            {@link Relation} name.
     * @param right
     *            Collection of {@link Executor}, which contains in right part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistException
     *             {@link Relation} with specified name does not exist.
     */
    List<RelationPair> getExecutorsRelationPairsRight(User user, String name, List<? extends Executor> right)
            throws RelationDoesNotExistException;

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left part contains executor from 'left' parameter.
     * 
     * @param user
     *            User, which perform operation.
     * @param name
     *            {@link Relation} name.
     * @param left
     *            Collection of {@link Executor}, which contains in left part of {@link RelationPair}.
     * @return List of {@link RelationPair}.
     * @throws RelationDoesNotExistException
     *             {@link Relation} with specified name does not exist.
     */
    List<RelationPair> getExecutorsRelationPairsLeft(User user, String name, List<? extends Executor> left)
            throws RelationDoesNotExistException;

    /**
     * To show on "manage_executor" page.
     */
    List<Relation> getRelationsContainingExecutorsOnLeft(User user, List<Executor> executors);

    /**
     * To show on "manage_executor" page.
     */
    List<Relation> getRelationsContainingExecutorsOnRight(User user, List<Executor> executors);
}
