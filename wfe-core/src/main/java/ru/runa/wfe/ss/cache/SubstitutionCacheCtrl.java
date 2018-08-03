package ru.runa.wfe.ss.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.DefaultCacheTransactionalExecutor;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;

/**
 * Cache control object for substitutions.
 */
class SubstitutionCacheCtrl extends BaseCacheCtrl<ManageableSubstitutionCache> implements SubstitutionCache {

    public SubstitutionCacheCtrl(boolean staleable) {
        super(staleable ? new StaleableSubstitutionCacheFactory() : new SubstitutionCacheFactory(), createListenObjectTypes());
    }

    @Override
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, loadIfRequired);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, false);
    }

    @Override
    public Set<Long> getSubstituted(Actor actor) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstituted(actor);
    }

    private static List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<>();
        result.add(new ListenObjectDefinition(Substitution.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(SubstitutionCriteria.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(ExecutorGroupMembership.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Executor.class, ListenObjectLogType.BECOME_DIRTY));
        return result;
    }

    /**
     * Static factory. It creates on the fly by demand and it state is always equals to database state. May leads to high delay if many executors and
     * substitutions is used. It's recommend to use {@link StaleableSubstitutionCacheFactory}.
     */
    private static class SubstitutionCacheFactory extends SMCacheFactory<ManageableSubstitutionCache> {

        SubstitutionCacheFactory() {
            super(Type.EAGER, null);
        }

        @Override
        protected ManageableSubstitutionCache createCacheImpl(CacheInitializationProcessContext context) {
            return new SubstitutionCacheImpl(true, false, null);
        }
    }

    /**
     * Non runtime factory. It creates on background and cache state may differs from database state for some time.
     */
    private static class StaleableSubstitutionCacheFactory extends SMCacheFactory<ManageableSubstitutionCache> {

        StaleableSubstitutionCacheFactory() {
            super(Type.LAZY_STALEABLE, new DefaultCacheTransactionalExecutor());
        }

        @Override
        protected ManageableSubstitutionCache createCacheStubImpl() {
            return new SubstitutionCacheImpl(false, true, null);
        }

        @Override
        protected ManageableSubstitutionCache createCacheImpl(CacheInitializationProcessContext context) {
            return new SubstitutionCacheImpl(true, true, context);
        }
    }
}
