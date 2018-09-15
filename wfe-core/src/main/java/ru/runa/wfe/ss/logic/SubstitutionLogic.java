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
package ru.runa.wfe.ss.logic;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.ss.cache.SubstitutionCache;
import ru.runa.wfe.ss.dao.SubstitutionCriteriaDao;
import ru.runa.wfe.ss.dao.SubstitutionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

/**
 * Created on 27.01.2006
 * 
 * @author Semochkin_v
 * @author Gordienko_m
 */
public class SubstitutionLogic extends CommonLogic {
    @Autowired
    private SubstitutionCache substitutionCacheCtrl;
    @Autowired
    private SubstitutionDao substitutionDao;
    @Autowired
    private SubstitutionCriteriaDao substitutionCriteriaDao;

    public void create(User user, Substitution substitution) {
        Actor actor = executorDao.getActor(substitution.getActorId());
        checkPermissionsOnExecutor(user, actor, Permission.UPDATE);
        List<Substitution> substitutions = substitutionDao.getByActorId(substitution.getActorId(), false);
        if (substitution.getPosition() == null) {
            // add last
            int position = substitutions.size() == 0 ? 0 : substitutions.get(0).getPosition() + 1;
            substitution.setPosition(position);
        } else {
            // insert at specified position mode
            boolean alreadyExistsInSamePosition = false;
            for (Substitution existing : substitutions) {
                if (Objects.equal(existing.getPosition(), substitution.getPosition())) {
                    alreadyExistsInSamePosition = true;
                    break;
                }
            }
            if (alreadyExistsInSamePosition) {
                for (Substitution existing : substitutions) {
                    if (existing.getPosition() >= substitution.getPosition()) {
                        log.info("Incrementing position in " + existing);
                        existing.setPosition(existing.getPosition() + 1);
                        substitutionDao.update(existing);
                        substitutionDao.flushPendingChanges();
                    }
                }
            }
        }
        log.info("Creating " + substitution);
        substitutionDao.create(substitution);
        substitutionDao.flushPendingChanges();
    }


    public List<Substitution> getSubstitutions(User user, Long actorId) {
        Actor actor = executorDao.getActor(actorId);
        checkPermissionsOnExecutor(user, actor, Permission.LIST);
        return substitutionDao.getByActorId(actorId, true);
    }


    public Substitution getSubstitution(User user, Long id) {
        return substitutionDao.getNotNull(id);
    }

    public void update(User user, Substitution substitution) {
        Actor actor = executorDao.getActor(substitution.getActorId());
        checkPermissionsOnExecutor(user, actor, Permission.UPDATE);
        List<Substitution> substitutions = substitutionDao.getByActorId(substitution.getActorId(), false);
        Integer oldPosition = null;
        Substitution substitutionWithNewPosition = null;
        if (substitution.getPosition() == null) {
            // add last
            int position = substitutions.size() == 0 ? 0 : substitutions.get(0).getPosition() + 1;
            substitution.setPosition(position);
        } else {
            // insert at specified position mode
            for (Substitution existing : substitutions) {
                if (Objects.equal(existing.getId(), substitution.getId())) {
                    oldPosition = existing.getPosition();
                } else if (Objects.equal(existing.getPosition(), substitution.getPosition())) {
                    substitutionWithNewPosition = existing;
                }
            }
        }
        if (Objects.equal(oldPosition, substitution.getPosition()) || substitutionWithNewPosition == null) {
            log.info("Saving " + substitution);
            substitutionDao.update(substitution);
        } else {
            log.info("Switching substitutions " + substitution + " <-> " + substitutionWithNewPosition);
            substitutionDao.delete(substitution.getId());
            substitutionDao.delete(substitutionWithNewPosition.getId());
            substitutionDao.flushPendingChanges();
            substitutionWithNewPosition.setId(null);
            substitution.setId(null);
            substitutionWithNewPosition.setPosition(oldPosition);
            log.info("Creating " + substitutionWithNewPosition);
            substitutionDao.create(substitutionWithNewPosition);
            log.info("Creating " + substitution);
            substitutionDao.create(substitution);
            substitutionDao.flushPendingChanges();
        }
    }

    // TODO clear code in update
    public void changePosition(User user, Substitution substitution, int newPosition) {
        Actor actor = executorDao.getActor(substitution.getActorId());
        checkPermissionsOnExecutor(user, actor, Permission.UPDATE);
        List<Substitution> substitutions = substitutionDao.getByActorId(substitution.getActorId(), false);
        Integer oldPosition = substitution.getPosition();
        Substitution substitutionWithNewPosition = null;
        for (Substitution existing : substitutions) {
            if (Objects.equal(existing.getPosition(), newPosition)) {
                substitutionWithNewPosition = existing;
            }
        }
        log.info("Switching substitutions " + substitution + " <-> " + substitutionWithNewPosition);
        substitutionDao.delete(substitution.getId());
        substitutionDao.delete(substitutionWithNewPosition.getId());
        substitutionDao.flushPendingChanges();
        substitutionWithNewPosition.setId(null);
        substitution.setId(null);
        substitution.setPosition(newPosition);
        substitutionWithNewPosition.setPosition(oldPosition);
        log.info("Creating " + substitutionWithNewPosition);
        substitutionDao.create(substitutionWithNewPosition);
        log.info("Creating " + substitution);
        substitutionDao.create(substitution);
        substitutionDao.flushPendingChanges();
    }

    private List<Actor> getSubstitutionActors(List<Substitution> substitutions) {
        Set<Long> actorIdSet = Sets.newHashSetWithExpectedSize(substitutions.size());
        for (Substitution substitution : substitutions) {
            actorIdSet.add(substitution.getActorId());
        }
        return executorDao.getActors(Lists.newArrayList(actorIdSet));
    }

    public void delete(User user, List<Long> substitutionIds) {
        List<Substitution> substitutions = substitutionDao.get(substitutionIds);
        if (substitutions.size() != substitutionIds.size()) {
            throw new SubstitutionDoesNotExistException(substitutionIds.toString());
        }
        List<Actor> actors = getSubstitutionActors(substitutions);
        checkPermissionsOnExecutors(user, actors, Permission.UPDATE);
        substitutionDao.delete(substitutionIds);
        for (Actor actor : actors) {
            fixPositionsForDeletedSubstitution(actor.getId());
        }
    }

    private void fixPositionsForDeletedSubstitution(Long actorId) {
        List<Substitution> actorSubstitutions = substitutionDao.getByActorId(actorId, true);
        for (int i = 0; i < actorSubstitutions.size(); i++) {
            Substitution substitution = actorSubstitutions.get(i);
            if (!Objects.equal(substitution.getPosition(), i)) {
                substitution.setPosition(i);
                substitutionDao.update(substitution);
            }
        }
    }

    public void delete(User user, Substitution substitution) {
        log.info("Deleting " + substitution);
        Actor actor = executorDao.getActor(substitution.getActorId());
        checkPermissionsOnExecutor(user, actor, Permission.UPDATE);
        substitutionDao.delete(substitution);
        fixPositionsForDeletedSubstitution(substitution.getActorId());
    }


    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor) {
        return substitutionCacheCtrl.getSubstitutors(actor, true);
    }


    public Set<Long> getSubstituted(Actor actor) {
        return substitutionCacheCtrl.getSubstituted(actor);
    }

    public void create(User user, SubstitutionCriteria criteria) {
        substitutionCriteriaDao.create(criteria);
    }


    public SubstitutionCriteria getCriteria(User user, Long id) {
        return substitutionCriteriaDao.getNotNull(id);
    }


    public SubstitutionCriteria getCriteria(User user, String name) {
        return substitutionCriteriaDao.getByName(name);
    }


    public List<SubstitutionCriteria> getAllCriterias(User user) {
        return substitutionCriteriaDao.getAll();
    }

    public void update(User user, SubstitutionCriteria substitutionsCriteria) {
        substitutionCriteriaDao.update(substitutionsCriteria);
    }

    public void deleteCriterias(User user, List<SubstitutionCriteria> criterias) {
        for (SubstitutionCriteria criteria : criterias) {
            substitutionCriteriaDao.delete(criteria);
        }
    }

    public void delete(User user, SubstitutionCriteria criteria) {
        substitutionCriteriaDao.delete(criteria);
    }


    public List<Substitution> getSubstitutionsByCriteria(User user, SubstitutionCriteria criteria) {
        return substitutionCriteriaDao.getSubstitutionsByCriteria(criteria);
    }
}
