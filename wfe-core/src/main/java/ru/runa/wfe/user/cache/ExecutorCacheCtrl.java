package ru.runa.wfe.user.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.factories.LazyInitializedCacheFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;

class ExecutorCacheCtrl extends BaseCacheCtrl<ManageableExecutorCache> implements ExecutorCache {

    ExecutorCacheCtrl() {
        super(
                new ExecutorCacheFactory(),
                new ArrayList<ListenObjectDefinition>() {{
                    add(new ListenObjectDefinition(Executor.class, ListenObjectLogType.ALL));
                    add(new ListenObjectDefinition(ExecutorGroupMembership.class, ListenObjectLogType.BECOME_DIRTY));
                }}
        );
    }

    @Override
    public Actor getActor(Long code) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getActor(code);
    }

    @Override
    public Executor getExecutor(String name) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutor(name);
    }

    @Override
    public Executor getExecutor(Long id) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutor(id);
    }

    @Override
    public Set<Executor> getGroupMembers(Group group) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getGroupMembers(group);
    }

    @Override
    public Set<Actor> getGroupActorsAll(Group group) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getGroupActorsAll(group);
    }

    @Override
    public Set<Group> getExecutorParents(Executor executor) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutorParents(executor);
    }

    @Override
    public Set<Group> getExecutorParentsAll(Executor executor) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutorParentsAll(executor);
    }

    @Override
    public <T extends Executor> VersionedCacheData<List<T>> getAllExecutor(Class<T> clazz, BatchPresentation batch) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getAllExecutor(clazz, batch);
    }

    @Override
    public <T extends Executor> void addAllExecutor(VersionedCacheData<List<T>> oldCachedData, Class<?> clazz, BatchPresentation batch,
            List<T> executors) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return;
        }
        cache.addAllExecutor(oldCachedData, clazz, batch, executors);
    }

    private static class ExecutorCacheFactory implements LazyInitializedCacheFactory<ManageableExecutorCache> {

        @Override
        public ManageableExecutorCache createProxy() {
            return new ExecutorCacheProxy();
        }

        @Override
        public ManageableExecutorCache buildCache(CacheInitializationContext<ManageableExecutorCache> context) {
            return new ExecutorCacheImpl(context);
        }
    }
}
