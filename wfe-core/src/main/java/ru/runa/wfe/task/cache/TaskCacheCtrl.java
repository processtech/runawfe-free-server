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
package ru.runa.wfe.task.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.TaskChangeListener;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.cache.SubstitutionCache;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.cache.ExecutorCache;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

class TaskCacheCtrl extends BaseCacheCtrl<TaskCacheImpl> implements TaskChangeListener, TaskCache {
    private static final String EXECUTOR_PROPERTY_NAME = "executor";

    @Autowired
    private SubstitutionCache substitutionCacheCtrl;
    @Autowired
    private ExecutorCache executorCacheCtrl;

    TaskCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public TaskCacheImpl buildCache() {
        return new TaskCacheImpl();
    }

    @Override
    public VersionedCacheData<List<WfTask>> getTasks(Long actorId, BatchPresentation batchPresentation) {
        TaskCacheImpl cache = CachingLogic.getCacheImplIfNotLocked(this);
        if (cache == null) {
            return null;
        }
        return cache.getTasks(actorId, batchPresentation);
    }

    @Override
    public void setTasks(VersionedCacheData<List<WfTask>> oldCachedData, Long actorId, BatchPresentation batchPresentation, List<WfTask> tasks) {
        TaskCacheImpl cache = CachingLogic.getCacheImplIfNotLocked(this);
        if (cache == null) {
            return;
        }
        cache.setTasks(oldCachedData, actorId, batchPresentation, tasks);
    }

    @Override
    public void doOnChange(ChangedObjectParameter changedObject) {
        if (changedObject.object instanceof Task || changedObject.object instanceof CurrentSwimlane) {
            clearObjectWithExecutorProperty(changedObject);
        } else if (changedObject.object instanceof ExecutorGroupMembership) {
            ExecutorGroupMembership membership = (ExecutorGroupMembership) changedObject.object;
            clearCacheForActors(membership.getExecutor(), changedObject.changeType);
        } else if (changedObject.object instanceof Actor) {
            if (changedObject.changeType == Change.UPDATE) {
                int activePropertyIndex = changedObject.getPropertyIndex("active");
                if (changedObject.previousState != null
                        && !Objects.equal(changedObject.previousState[activePropertyIndex], changedObject.currentState[activePropertyIndex])) {
                    // TODO clear cache for affected actors only
                    uninitialize(changedObject);
                }
            }
        } else if (changedObject.object instanceof Substitution) {
            Substitution substitution = (Substitution) changedObject.object;
            Actor actor = (Actor) executorCacheCtrl.getExecutor(substitution.getActorId());
            if (actor != null) {
                clearCacheForActors(actor, changedObject.changeType);
            } else {
                uninitialize(changedObject);
            }
        } else if (changedObject.object instanceof SubstitutionCriteria) {
            // TODO clear cache for affected actors only
            uninitialize(changedObject);
        } else {
            throw new InternalApplicationException("Unexpected object " + changedObject.object);
        }
    }

    /**
     * Clear caches for object, which contains executor property.
     * 
     * @param object
     *            Changed object.
     * @param change
     *            Change type.
     * @param currentState
     *            Current state of changed object.
     * @param previousState
     *            Previous state of changed object.
     * @param propertyNames
     *            Properties names of changed object.
     */
    private void clearObjectWithExecutorProperty(ChangedObjectParameter changedObject) {
        int idx = changedObject.getPropertyIndex(EXECUTOR_PROPERTY_NAME);
        clearCacheForActors((Executor) changedObject.currentState[idx], changedObject.changeType);
        if (changedObject.previousState != null) {
            clearCacheForActors((Executor) changedObject.previousState[idx], changedObject.changeType);
        }
        return;
    }

    /**
     * Clears task lists, affected by changes in executor.
     * 
     * @param executor
     *            Changed executor.
     * @param change
     *            Change type.
     */
    private void clearCacheForActors(Executor executor, Change change) {
        TaskCacheImpl cache = getCache();
        if (cache == null || executor == null) {
            return;
        }
        log.debug("Clearing cache for " + executor + " due to " + change);
        Set<Actor> actors = Sets.newHashSet();
        if (!fillAffectedActors(executor, change, actors)) {
            return;
        }
        for (Actor actor : actors) {
            cache.clearActorTasks(actor.getId());
            Map<Substitution, Set<Long>> substitutors = substitutionCacheCtrl.tryToGetSubstitutors(actor);
            if (substitutors == null) {
                uninitialize(this, change);
                return;
            }
            for (Map.Entry<Substitution, Set<Long>> entry : substitutors.entrySet()) {
                if (entry.getKey() instanceof TerminatorSubstitution) {
                    continue;
                }
                for (Long substitutorActor : entry.getValue()) {
                    cache.clearActorTasks(substitutorActor);
                }
            }
        }
    }

    /**
     * Fill set of actors, which task lists may be affected by changing executor.
     * 
     * @param changedExecutor
     *            Executor, which may affect other executors.
     * @param change
     *            Change type.
     * @param affectedActors
     *            Set of affected actors to fill.
     * @return true, if affected actors is filled and false if no other actions required.
     */
    private boolean fillAffectedActors(Executor changedExecutor, Change change, Set<Actor> affectedActors) {
        if (changedExecutor instanceof Group) {
            Set<Actor> cached = executorCacheCtrl.getGroupActorsAll((Group) changedExecutor);
            if (cached == null) {
                log.warn("No group actors found in cache for " + changedExecutor);
                uninitialize(changedExecutor, change);
                return false;
            }
            affectedActors.addAll(cached);
        } else {
            affectedActors.add((Actor) changedExecutor);
        }
        return true;
    }
}
