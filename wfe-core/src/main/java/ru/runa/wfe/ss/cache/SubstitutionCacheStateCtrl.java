package ru.runa.wfe.ss.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.transaction.Transaction;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.factories.NonRuntimeCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;

class SubstitutionCacheStateCtrl extends BaseCacheCtrl<ManageableSubstitutionCache> implements SubstitutionCache {

    public SubstitutionCacheStateCtrl() {
        super(new NonRuntimeSubstitutionCacheFactory(), createListenObjectTypes());
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, loadIfRequired);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        Transaction transaction = Utils.getTransaction();
        if (transaction == null) {
            return null;
        }
        SubstitutionCache cache = stateMachine.getCacheQuick(transaction);
        if (cache != null) {
            return cache.getSubstitutors(actor, false);
        } else {
            return null;
        }
    }

    @Override
    public Set<Long> getSubstituted(Actor actor) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstituted(actor);
    }

    private static final List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<ListenObjectDefinition>();
        result.add(new ListenObjectDefinition(Substitution.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(SubstitutionCriteria.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(ExecutorGroupMembership.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Executor.class, ListenObjectLogType.BECOME_DIRTY));
        return result;
    }

    private static class SubstitutionCacheFactory implements StaticCacheFactory<ManageableSubstitutionCache> {

        @Override
        public ManageableSubstitutionCache buildCache() {
            return new SubstitutionCacheStateImpl(true);
        }
    }

    private static class NonRuntimeSubstitutionCacheFactory implements NonRuntimeCacheFactory<ManageableSubstitutionCache> {

        @Override
        public ManageableSubstitutionCache createProxy() {
            return new SubstitutionCacheStateImpl(false);
        }

        @Override
        public ManageableSubstitutionCache buildCache(CacheInitializationContext<ManageableSubstitutionCache> context) {
            return new SubstitutionCacheStateImpl(true);
        }
    }
}
