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

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContextStub;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.dao.SubstitutionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * Cache implementation for substitutions.
 */
@CommonsLog
public class SubstitutionCacheImpl extends BaseCacheImpl implements ManageableSubstitutionCache {

    /**
     * EHCache name.
     */
    public static final String substitutorsName = "ru.runa.wfe.ss.cache.substitutors";

    /**
     * EHCache name.
     */
    public static final String substitutedName = "ru.runa.wfe.ss.cache.substituted";

    /**
     * Maps from actor id to it substitution rules and actors, which may substitute key actor by rule.
     */
    private final Cache<Long, TreeMap<Substitution, HashSet<Long>>> actorToSubstitutorsCache;

    /**
     * Map from actor id to all actors, which may be substituted by key actor. Only inactive substituted actors added as substituted (no need to check
     * if it active).
     */
    private final Cache<Long, HashSet<Long>> actorToSubstitutedCache;

    /**
     * Flag, equals true, if cache is not runtime and it state may different from database state and false otherwise.
     */
    private final boolean isStaleable;

    /**
     * Creates cache implementation for substitutions.
     *
     * @param fullInitialization
     *            Flag, equals true, if cache must be fully initialized and false, if cache must be empty (no initialization).
     * @param isStaleable
     *            Flag, equals true, if cache is not runtime and it state may different from database state and false otherwise.
     * @param initializationContext
     *            Cache initialization context.
     */
    public SubstitutionCacheImpl(boolean fullInitialization, boolean isStaleable, CacheInitializationProcessContext initializationContext) {
        if (initializationContext == null) {
            initializationContext = new CacheInitializationProcessContextStub();
        }
        this.isStaleable = isStaleable;
        actorToSubstitutorsCache = createCache(substitutorsName, true);
        actorToSubstitutedCache = createCache(substitutedName, true);
        if (!fullInitialization) {
            return;
        }
        Map<Long, TreeMap<Substitution, HashSet<Long>>> actorToSubstitutors = getMapActorToSubstitutors(initializationContext);
        Map<Long, HashSet<Long>> actorToSubstituted = getMapActorToSubstituted(actorToSubstitutors, initializationContext);
        if (!initializationContext.isInitializationStillRequired()) {
            return;
        }
        actorToSubstitutorsCache.putAll(actorToSubstitutors);
        actorToSubstitutedCache.putAll(actorToSubstituted);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired) {
        if (actor.isActive()) {
            return new TreeMap<>();
        }
        TreeMap<Substitution, HashSet<Long>> result = actorToSubstitutorsCache.get(actor.getId());
        if (result != null) {
            return new TreeMap<>(result);
        }
        return new TreeMap<>();
    }

    @Override
    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        return null;
    }

    @Override
    public HashSet<Long> getSubstituted(Actor actor) {
        HashSet<Long> result = actorToSubstitutedCache.get(actor.getId());
        if (result != null) {
            return result;
        }
        return new HashSet<>();
    }

    private static Map<Long, TreeMap<Substitution, HashSet<Long>>> getMapActorToSubstitutors(
            CacheInitializationProcessContext initializationContext) {
        Map<Long, TreeMap<Substitution, HashSet<Long>>> result = Maps.newHashMap();
        final ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
        try {
            final SubstitutionDao substitutionDao = ApplicationContextFactory.getSubstitutionDAO();
            for (Substitution substitution : substitutionDao.getAll()) {
                if (!initializationContext.isInitializationStillRequired()) {
                    return result;
                }
                try {
                    Long actorId;
                    try {
                        actorId = executorDao.getActor(substitution.getActorId()).getId();
                    } catch (ExecutorDoesNotExistException e) {
                        log.error("in " + substitution + ": " + e);
                        continue;
                    }
                    if (!substitution.isEnabled()) {
                        continue;
                    }
                    TreeMap<Substitution, HashSet<Long>> subDescr = result.get(actorId);
                    if (subDescr == null) {
                        subDescr = new TreeMap<>();
                        result.put(actorId, subDescr);
                    }
                    if (substitution instanceof TerminatorSubstitution) {
                        subDescr.put(substitution, null);
                        continue;
                    }
                    List<? extends Executor> executors = SwimlaneInitializerHelper.evaluate(substitution.getOrgFunction(), null);
                    HashSet<Long> substitutors = new HashSet<>();
                    for (Executor sub : executors) {
                        if (sub instanceof Actor) {
                            substitutors.add(sub.getId());
                        } else {
                            for (Actor groupActor : executorDao.getGroupActors((Group) sub)) {
                                substitutors.add(groupActor.getId());
                            }
                        }
                    }
                    subDescr.put(substitution, substitutors);
                } catch (Exception e) {
                    log.error("Error in " + substitution, e);
                }
            }
        } catch (Throwable th) {
            log.error("in substitution", th);
        }
        return result;
    }

    private static Map<Long, HashSet<Long>> getMapActorToSubstituted(Map<Long, TreeMap<Substitution, HashSet<Long>>> mapActorToSubstitutors,
            CacheInitializationProcessContext initializationContext) {
        Map<Long, HashSet<Long>> result = new HashMap<>();
        final ExecutorDao executorDao = ApplicationContextFactory.getExecutorDao();
        for (Map.Entry<Long, TreeMap<Substitution, HashSet<Long>>> entry1 : mapActorToSubstitutors.entrySet()) {
            final Long substitutedId = entry1.getKey();
            // TODO why is it here?
            if (!initializationContext.isInitializationStillRequired()) {
                return result;
            }
            try {
                Actor substitutedActor = executorDao.getActor(substitutedId);
                if (substitutedActor.isActive()) {
                    continue;
                }
                for (HashSet<Long> substitutors : entry1.getValue().values()) {
                    if (substitutors == null) {
                        continue;
                    }
                    for (Long substitutor : substitutors) {
                        HashSet<Long> set = result.get(substitutor);
                        if (set == null) {
                            set = new HashSet<>();
                            result.put(substitutor, set);
                        }
                        set.add(substitutedActor.getId());
                    }
                }
            } catch (ExecutorDoesNotExistException e) {
                // Do nothing.
            }
        }
        return result;
    }

    @Override
    public CacheImplementation unlock() {
        return null;
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return isStaleable;
    }
}
