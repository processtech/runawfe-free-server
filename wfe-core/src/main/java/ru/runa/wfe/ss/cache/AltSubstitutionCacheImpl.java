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
package ru.runa.wfe.ss.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.dao.SubstitutionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

class AltSubstitutionCacheImpl extends BaseCacheImpl implements SubstitutionCache {
    public static final String substitutorsName = "ru.runa.wfe.ss.cache.substitutors";
    public static final String substitutedName = "ru.runa.wfe.ss.cache.substituted";
    private final Cache<Long, TreeMap<Substitution, HashSet<Long>>> actorToSubstitutorsCache;
    private final Cache<Long, HashSet<Long>> actorToSubstitutedCache;
    private final ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
    private final SubstitutionDAO substitutionDAO = ApplicationContextFactory.getSubstitutionDAO();

    public AltSubstitutionCacheImpl() {
        actorToSubstitutorsCache = createCache(substitutorsName, true);
        actorToSubstitutedCache = createCache(substitutedName, true);
        for (Actor actor : executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged())) {
            if (!actor.isActive()) {
                loadCacheFor(actor.getId());
            }
        }
    }

    private HashSet<Long> loadCacheFor(Long actorId, Substitution substitution) {
        HashSet<Long> substitutors = Sets.newHashSet();
        if (!substitution.isEnabled()) {
            return substitutors;
        }
        if (substitution instanceof TerminatorSubstitution) {
            return substitutors;
        }
        try {
            List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(substitution.getOrgFunction(), null);
            for (Executor executor : executors) {
                if (executor instanceof Actor) {
                    substitutors.add(executor.getId());
                } else {
                    for (Actor groupActor : executorDAO.getGroupActors((Group) executor)) {
                        substitutors.add(groupActor.getId());
                    }
                }
            }
            for (Long substitutor : substitutors) {
                HashSet<Long> substituted = actorToSubstitutedCache.get(substitutor);
                if (substituted == null) {
                    substituted = Sets.newHashSet();
                    actorToSubstitutedCache.put(substitutor, substituted);
                }
                substituted.add(actorId);
            }
        } catch (Exception e) {
            log.error("Error in " + substitution, e);
        }
        return substitutors;
    }

    private void loadCacheFor(Long actorId) {
        TreeMap<Substitution, HashSet<Long>> result = Maps.newTreeMap();
        for (Substitution substitution : substitutionDAO.getByActorId(actorId, true)) {
            HashSet<Long> substitutors = loadCacheFor(actorId, substitution);
            result.put(substitution, substitutors);
        }
        actorToSubstitutorsCache.put(actorId, result);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired) {
        TreeMap<Substitution, HashSet<Long>> result = actorToSubstitutorsCache.get(actor.getId());
        return result != null ? new TreeMap<Substitution, Set<Long>>(result) : new TreeMap<Substitution, Set<Long>>();
    }

    @Override
    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        return null;
    }

    @Override
    public HashSet<Long> getSubstituted(Actor actor) {
        HashSet<Long> result = actorToSubstitutedCache.get(actor.getId());
        return result != null ? result : new HashSet<Long>();
    }

    public void onSubstitutionChange(Actor actor, Substitution substitution, Change change) {
        if (actor != null && !actor.isActive()) {
            if (change == Change.CREATE) {
                loadCacheFor(actor.getId(), substitution);
            }
            if (change == Change.UPDATE) {
                // TODO implement additional logic
            }
            if (change == Change.DELETE) {
                // TODO implement additional logic
            }
        }
    }

    public void onActorNameChange(Actor actor, Change change) {
    }

    public void onActorStatusChange(Actor actor, Change change) {
        TreeMap<Substitution, HashSet<Long>> substitutors = actorToSubstitutorsCache.get(actor.getId());
        if (substitutors == null) {
            // status change: active -> inactive OR new actor has been created
            if (!actor.isActive()) {
                loadCacheFor(actor.getId());
            }
        } else {
            // status change: inactive -> active OR actor deletion
            if (actor.isActive() || change == Change.DELETE) {
                for (Map.Entry<Substitution, HashSet<Long>> entry : substitutors.entrySet()) {
                    if (entry.getKey() instanceof TerminatorSubstitution) {
                        continue;
                    }
                    for (Long substitutor : entry.getValue()) {
                        HashSet<Long> substituted = actorToSubstitutedCache.get(substitutor);
                        substituted.remove(actor.getId());
                    }
                }
                actorToSubstitutorsCache.remove(actor.getId());
            }
        }
    }

    @Override
    public CacheImplementation unlock() {
        return null;
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
