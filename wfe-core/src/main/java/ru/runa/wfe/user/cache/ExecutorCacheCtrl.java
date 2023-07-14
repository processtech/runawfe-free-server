package ru.runa.wfe.user.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.transaction.Transaction;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.VersionedCacheData;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;

@Component
public class ExecutorCacheCtrl extends BaseCacheCtrl<ManageableExecutorCache> {

    ExecutorCacheCtrl() {
        super(new ExecutorCacheFactory(), new ArrayList<ListenObjectDefinition>() {
            {
                add(new ListenObjectDefinition(Actor.class));
                add(new ListenObjectDefinition(Group.class));
                add(new ListenObjectDefinition(ExecutorGroupMembership.class));
            }
        });
    }

    @Override
    public boolean onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        if (changedObject.object instanceof TemporaryGroup) {
            return false;
        }
        if (changedObject.object instanceof ExecutorGroupMembership
                && ((ExecutorGroupMembership) changedObject.object).getGroup() instanceof TemporaryGroup) {
            return false;
        }
        return super.onChange(transaction, changedObject);
    }
    /**
     * Return {@link Actor} with specified code, or null, if such actor not exists or cache is not valid.
     *
     * @param code
     *            Actor code.
     * @return {@link Actor} with specified code.
     */
    public Actor getActor(Long code) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getActor(code);
    }

    /**
     * Return {@link Executor} with specified name, or null, if such executor not exists or cache is not valid.
     *
     * @param name
     *            Executor name.
     * @return {@link Executor} with specified name.
     */
    public Executor getExecutor(String name) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutor(name);
    }

    /**
     * Return {@link Executor} with specified id, or null, if such executor not exists or cache is not valid.
     *
     * @param id
     *            Executor identity.
     * @return {@link Executor} with specified identity.
     */
    public Executor getExecutor(Long id) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutor(id);
    }

    /**
     * Return first level {@link Group} members. Only {@link Executor}, which directly contains in {@link Group} is returning. No recursive group
     * search performs. May return null, if cache is not valid.
     *
     * @param group
     *            {@link Group}, which members will be returned.
     * @return First level {@link Group} members.
     */
    public Set<Executor> getGroupMembers(Group group) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getGroupMembers(group);
    }

    /**
     * Return all {@link Actor} members of specified {@link Group} and all her subgroups. {@link Actor} members searching recursive and all actors
     * from subgroups is also contains in result set. May return null, if cache is not valid.
     *
     * @param group
     *            {@link Group}, which actor members will be returned.
     * @return All {@link Actor} members of specified {@link Group} and all her subgroups.
     */
    public Set<Actor> getGroupActorsAll(Group group) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getGroupActorsAll(group);
    }

    /**
     * Return all {@link Group}, which contains specified {@link Executor} as first level member. May return null, if cache is not valid.
     *
     * @param executor
     *            {@link Executor}, which parents will be returned.
     * @return All {@link Group}, which contains specified {@link Executor} as first level member.
     */
    public Set<Group> getExecutorParents(Executor executor) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutorParents(executor);
    }

    /**
     * Return all {@link Group}, which contains specified {@link Executor} as member (direct or recursive by subgroups). May return null, if cache is
     * not valid.
     *
     * @param executor
     *            {@link Executor}, which parents will be returned.
     * @return All {@link Group}, which contains specified {@link Executor} as member.
     */
    public Set<Group> getExecutorParentsAll(Executor executor) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getExecutorParentsAll(executor);
    }

    /**
     * Return all {@link Executor} of specified class according to {@link BatchPresentation}. May return null, if executor list for specified class
     * and presentation wasn't set yet (with addAllExecutor()). May return null, if cache is not valid.
     *
     * @param <T>
     *            Type of returned objects. Must be {@link Executor} or it subclass.
     * @param clazz
     *            Type of returned objects. Must be {@link Executor} or it subclass.
     * @param batch
     *            {@link BatchPresentation} to sort/filter result.
     * @return All {@link Executor} of specified class according to {@link BatchPresentation}.
     */
    public <T extends Executor> VersionedCacheData<List<T>> getAllExecutor(Class<T> clazz, BatchPresentation batch) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return null;
        }
        return cache.getAllExecutor(clazz, batch);
    }

    /**
     * Set {@link Executor} list for specified class and {@link BatchPresentation}.
     *
     * @param oldCachedData
     *            Old state for caching data.
     * @param clazz
     *            Type of executors. Must be {@link Executor} or it subclass.
     * @param batch
     *            Presentation for executors.
     * @param executors
     *            Executor list for specified class and presentation. Will be returned on next {@link #getAllExecutor(Class, BatchPresentation)} call
     *            with specified class and presentation.
     */
    public <T extends Executor> void addAllExecutor(VersionedCacheData<List<T>> oldCachedData, Class<?> clazz, BatchPresentation batch,
            List<T> executors) {
        ManageableExecutorCache cache = CachingLogic.getCacheImplIfNotLocked(stateMachine);
        if (cache == null) {
            return;
        }
        cache.addAllExecutor(oldCachedData, clazz, batch, executors);
    }

    private static class ExecutorCacheFactory extends SMCacheFactory<ManageableExecutorCache> {

        ExecutorCacheFactory() {
            super(Type.LAZY);
        }

        @Override
        protected ManageableExecutorCache createCacheStubImpl() {
            return new ExecutorCacheStub();
        }

        @Override
        protected ManageableExecutorCache createCacheImpl(CacheInitializationProcessContext context) {
            return new ExecutorCacheImpl(context);
        }

    }
}
