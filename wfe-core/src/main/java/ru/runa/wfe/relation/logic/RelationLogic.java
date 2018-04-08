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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.commons.logic.IgnoreGrantedPermissionCallback;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

/**
 * Relation logic.
 * 
 * @author Konstantinov Aleksey 12.02.2012
 */
public class RelationLogic extends CommonLogic {
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;

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
        Relation relation = relationDAO.getNotNull(relationId);
        checkPermissionAllowed(user, relation, Permission.UPDATE_RELATION);
        return relationPairDAO.addRelationPair(relation, left, right);
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
        checkPermissionAllowed(user, RelationsGroupSecure.INSTANCE, Permission.UPDATE_RELATION);
        return relationDAO.create(relation);
    }

    public Relation updateRelation(User user, Relation relation) {
        checkPermissionAllowed(user, RelationsGroupSecure.INSTANCE, Permission.UPDATE_RELATION);
        return relationDAO.update(relation);
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
        checkPermissionAllowed(user, RelationsGroupSecure.INSTANCE, Permission.READ);
        return (List<Relation>) permissionDAO.getPersistentObjects(user, batchPresentation, Permission.READ,
                new SecuredObjectType[] { SecuredObjectType.RELATION }, false);
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which right part contains executor from 'right' parameter.
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
        List<RelationPair> result = new ArrayList<>();
        Relation relation = relationName != null ? relationDAO.getNotNull(relationName) : null;
        List<RelationPair> loadedPairs = relationPairDAO.getExecutorsRelationPairsRight(relation, right);
        Set<Relation> allowedRelations = getRelationsWithReadPermission(user, loadedPairs);
        for (RelationPair pair : loadedPairs) {
            if (allowedRelations.contains(pair.getRelation())) {
                result.add(pair);
            }
        }
        return result;
    }

    /**
     * Return {@link RelationPair} for specified {@link Relation}, which left part contains executor from 'left' parameter.
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
        List<RelationPair> result = new ArrayList<>();
        Relation relation = relationName != null ? relationDAO.getNotNull(relationName) : null;
        List<RelationPair> loadedPairs = relationPairDAO.getExecutorsRelationPairsLeft(relation, left);
        Set<Relation> allowedRelations = getRelationsWithReadPermission(user, loadedPairs);
        for (RelationPair pair : loadedPairs) {
            if (allowedRelations.contains(pair.getRelation())) {
                result.add(pair);
            }
        }
        return result;
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
        checkPermissionAllowed(user, relationDAO.getNotNull(relationName), Permission.READ);
        return relationDAO.getNotNull(relationName);
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
        Relation relation = relationDAO.getNotNull(relationId);
        checkPermissionAllowed(user, relation, Permission.READ);
        return relation;
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
        Relation relation = relationDAO.getNotNull(relationName);
        checkPermissionAllowed(user, relation, Permission.READ);
        return relationPairDAO.getRelationPairs(relation, batchPresentation);
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
        Relation relation = relationDAO.getNotNull(relationId);
        checkPermissionAllowed(user, relation, Permission.READ);
        return relationPairDAO.getRelationPairs(relation, batchPresentation);
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
        RelationPair relationPair = relationPairDAO.getNotNull(relationPairId);
        checkPermissionAllowed(user, relationPair.getRelation(), Permission.UPDATE_RELATION);
        permissionDAO.deleteAllPermissions(relationPair);
        relationPairDAO.delete(relationPair);
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
        checkPermissionAllowed(user, RelationsGroupSecure.INSTANCE, Permission.UPDATE_RELATION);
        permissionDAO.deleteAllPermissions(getRelation(user, relationId));
        relationDAO.delete(relationId);
    }

    /**
     * Returns set of {@link Relation} from relationPairs parameter with Read permission for current user.
     * 
     * @param user
     *            user, which perform operation.
     * @param relationPairs
     *            Relation pairs, from which {@link Relation} extracted.
     * @return {@link Relation}'s with READ permission.
     */
    private Set<Relation> getRelationsWithReadPermission(User user, List<RelationPair> relationPairs) {
        final Set<Relation> result = new HashSet<>();
        for (RelationPair relationPair : relationPairs) {
            result.add(relationPair.getRelation());
        }
        isPermissionAllowed(user, new ArrayList<>(result), Permission.READ, new IgnoreGrantedPermissionCallback() {
            @Override
            public void OnPermissionDenied(Identifiable identifiable) {
                result.remove(identifiable);
            }
        });
        return result;
    }
}
