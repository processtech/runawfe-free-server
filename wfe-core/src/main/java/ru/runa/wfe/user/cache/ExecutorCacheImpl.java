package ru.runa.wfe.user.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.SerializationUtils;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContextStub;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;

/**
 * Cache for executors.
 *
 * @author Konstantinov Aleksey
 */
class ExecutorCacheImpl extends BaseCacheImpl implements ManageableExecutorCache {
    /* Ehcaches names. */
    public static final String actorsByCodesName = "ru.runa.wfe.user.cache.actorsByCodes";
    public static final String executorsByIdName = "ru.runa.wfe.user.cache.executorsById";
    public static final String executorsByNameName = "ru.runa.wfe.user.cache.executorsByName";
    public static final String groupMembersName = "ru.runa.wfe.user.cache.groupMembers";
    public static final String executorParentsName = "ru.runa.wfe.user.cache.executorParents";
    public static final String allGroupActorsName = "ru.runa.wfe.user.cache.allGroupActors";
    public static final String allExecutorGroupsName = "ru.runa.wfe.user.cache.allExecutorGroups";
    public static final String allExecutorsListsName = "ru.runa.wfe.user.cache.allExecutorsLists";

    /* Caches implementation. */
    private final Cache<Long, Actor> codeToActorCache;
    private final Cache<Long, Executor> idToExecutorCache;
    private final Cache<String, Executor> nameToExecutorCache;
    private final Cache<Long, HashSet<Executor>> groupToMembersCache;
    private final Cache<Long, HashSet<Group>> executorToParentGroupsCache;
    private final Cache<Long, HashSet<Actor>> groupToAllActorMembersCache;
    private final Cache<Long, HashSet<Group>> executorToAllParentGroupsCache;
    private final Cache<Class<?>, ConcurrentHashMap<BatchPresentationFieldEquals, List<Executor>>> batchAllExecutors;

    public ExecutorCacheImpl() {
        this(new CacheInitializationProcessContextStub());
    }

    public ExecutorCacheImpl(CacheInitializationProcessContext context) {
        codeToActorCache = createCache(actorsByCodesName);
        idToExecutorCache = createCache(executorsByIdName);
        nameToExecutorCache = createCache(executorsByNameName);
        groupToMembersCache = createCache(groupMembersName);
        executorToParentGroupsCache = createCache(executorParentsName);
        groupToAllActorMembersCache = createCache(allGroupActorsName);
        executorToAllParentGroupsCache = createCache(allExecutorGroupsName);
        batchAllExecutors = createCache(allExecutorsListsName);
        List<Executor> allExecutors = getAllExecutors();
        if (!context.isInitializationStillRequired()) {
            return;
        }
        List<ExecutorGroupMembership> memberships = getAllMemberships();
        if (!context.isInitializationStillRequired()) {
            return;
        }
        for (Executor executor : allExecutors) {
            addExecutorToCaches(executor);
            if (!context.isInitializationStillRequired()) {
                return;
            }
        }
        fillGroupMembersCaches(context, memberships, allExecutors);
    }

    @Override
    public Actor getActor(Long code) {
        return (Actor) SerializationUtils.clone(codeToActorCache.get(code));
    }

    @Override
    public Executor getExecutor(String name) {
        return (Executor) SerializationUtils.clone(nameToExecutorCache.get(name));
    }

    @Override
    public Executor getExecutor(Long id) {
        return (Executor) SerializationUtils.clone(idToExecutorCache.get(id));
    }

    @Override
    public Set<Executor> getGroupMembers(Group group) {
        return (Set<Executor>) SerializationUtils.clone(groupToMembersCache.get(group.getId()));
    }

    @Override
    public Set<Actor> getGroupActorsAll(Group group) {
        return (Set<Actor>) SerializationUtils.clone(groupToAllActorMembersCache.get(group.getId()));
    }

    @Override
    public Set<Group> getExecutorParents(Executor executor) {
        return (Set<Group>) SerializationUtils.clone(executorToParentGroupsCache.get(executor.getId()));
    }

    @Override
    public Set<Group> getExecutorParentsAll(Executor executor) {
        return (Set<Group>) SerializationUtils.clone(executorToAllParentGroupsCache.get(executor.getId()));
    }

    @Override
    public <T extends Executor> VersionedCacheData<List<T>> getAllExecutor(Class<T> clazz, BatchPresentation batch) {
        synchronized (this) {
            ConcurrentHashMap<BatchPresentationFieldEquals, List<Executor>> map = batchAllExecutors.get(clazz);
            if (map == null) {
                return getVersionnedData(null);
            }
            List<Executor> cachedExecutors = map.get(new BatchPresentationFieldEquals(batch));
            if (cachedExecutors == null) {
                return getVersionnedData(null);
            }
            return getVersionnedData((List<T>) Collections.unmodifiableList(cachedExecutors));
        }
    }

    @Override
    public <T extends Executor> void addAllExecutor(VersionedCacheData<List<T>> oldCachedData, Class<?> clazz, BatchPresentation batch,
            List<T> executors) {
        if (!mayUpdateVersionnedData(oldCachedData)) {
            return;
        }
        synchronized (this) {
            ConcurrentHashMap<BatchPresentationFieldEquals, List<Executor>> map = batchAllExecutors.get(clazz);
            if (map == null) {
                map = new ConcurrentHashMap<>();
            }
            List<Executor> result = new ArrayList<>();
            for (Executor executor : executors) {
                result.add(executor);
            }
            map.put(new BatchPresentationFieldEquals(batch), result);
            batchAllExecutors.put(clazz, map);
        }
    }

    private void addExecutorToCaches(Executor executor) {
        idToExecutorCache.put(executor.getId(), executor);
        nameToExecutorCache.put(executor.getName(), executor);
        if (executor instanceof Actor) {
            codeToActorCache.put(((Actor) executor).getCode(), (Actor) executor);
        }
    }

    private <Key extends Serializable, ValueInSet> Set<ValueInSet> getCollectionFromMap(Cache<Key, HashSet<ValueInSet>> map, Key key) {
        HashSet<ValueInSet> retVal = map.get(key);
        if (retVal == null) {
            retVal = new HashSet<>();
            map.put(key, retVal);
        }
        return retVal;
    }

    private void fillGroupMembersCaches(CacheInitializationProcessContext context, List<ExecutorGroupMembership> memberships,
            List<Executor> executors) {
        for (ExecutorGroupMembership membership : memberships) {
            getCollectionFromMap(groupToMembersCache, membership.getGroup().getId()).add(membership.getExecutor());
            getCollectionFromMap(executorToParentGroupsCache, membership.getExecutor().getId()).add(membership.getGroup());
            if (!context.isInitializationStillRequired()) {
                return;
            }
        }
        for (Executor executor : executors) {
            if (executorToParentGroupsCache.get(executor.getId()) == null) {
                executorToParentGroupsCache.put(executor.getId(), new HashSet<>());
            }
            if (executor instanceof Group && groupToMembersCache.get(executor.getId()) == null) {
                groupToMembersCache.put(executor.getId(), new HashSet<>());
            }
            if (!context.isInitializationStillRequired()) {
                return;
            }
        }

        for (Executor executor : executors) {
            fillAllParentsCache(executorToAllParentGroupsCache, nameToExecutorCache.get(executor.getName()), executorToParentGroupsCache);
            if (executor instanceof Group) {
                fillActorMembersCache(groupToAllActorMembersCache, (Group) (nameToExecutorCache.get(executor.getName())), groupToMembersCache);
            }
            if (!context.isInitializationStillRequired()) {
                return;
            }
        }
    }

    private Set<Group> fillAllParentsCache(Cache<Long, HashSet<Group>> cache, Executor executor, Cache<Long, HashSet<Group>> mapExecutorToParents) {
        HashSet<Group> executorGroups = cache.get(executor.getId());
        if (executorGroups != null) {
            return executorGroups;
        }
        executorGroups = new HashSet<>();
        cache.put(executor.getId(), executorGroups);
        if (mapExecutorToParents.get(executor.getId()) != null) {
            for (Group group : mapExecutorToParents.get(executor.getId())) {
                executorGroups.add(group);
                executorGroups.addAll(fillAllParentsCache(cache, group, mapExecutorToParents));
            }
        }
        return executorGroups;
    }

    private Set<Actor> fillActorMembersCache(Cache<Long, HashSet<Actor>> cache, Group group, Cache<Long, HashSet<Executor>> mapGroupToMembers) {
        HashSet<Actor> actorMembers = cache.get(group.getId());
        if (actorMembers != null) {
            return actorMembers;
        }
        actorMembers = new HashSet<>();
        cache.put(group.getId(), actorMembers);
        if (mapGroupToMembers.get(group.getId()) != null) {
            for (Executor ex : mapGroupToMembers.get(group.getId())) {
                if (ex instanceof Actor) {
                    actorMembers.add((Actor) ex);
                } else {
                    actorMembers.addAll(fillActorMembersCache(cache, (Group) ex, mapGroupToMembers));
                }
            }
        }
        return actorMembers;
    }

    private List<ExecutorGroupMembership> getAllMemberships() {
        return ApplicationContextFactory.getCurrentSession()
                .createSQLQuery("SELECT * FROM EXECUTOR_GROUP_MEMBER WHERE GROUP_ID IN (SELECT ID FROM EXECUTOR WHERE DISCRIMINATOR IN ('Y', 'N'))")
                .addEntity(ExecutorGroupMembership.class).list();
    }

    private List<Executor> getAllExecutors() {
        return ApplicationContextFactory.getCurrentSession().createSQLQuery("SELECT * FROM EXECUTOR WHERE DISCRIMINATOR IN ('Y', 'N')")
                .addEntity(Executor.class).list();
    }

    private static class BatchPresentationFieldEquals {
        private final BatchPresentation batchPresentation;

        BatchPresentationFieldEquals(BatchPresentation batchPresentation) {
            this.batchPresentation = batchPresentation.clone();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ExecutorCacheImpl.BatchPresentationFieldEquals) {
                return batchPresentation.fieldEquals(((ExecutorCacheImpl.BatchPresentationFieldEquals) obj).batchPresentation);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return batchPresentation.hashCode();
        }
    }

    @Override
    public boolean onChange(ChangedObjectParameter changedObject) {
        return false;
    }
}
