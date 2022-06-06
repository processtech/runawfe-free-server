package ru.runa.wfe.ss.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.transaction.Transaction;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationProcessContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.DefaultCacheTransactionalExecutor;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorGroupMembership;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;

/**
 * Cache control object for substitutions.
 */
@Component
public class SubstitutionCacheCtrl extends BaseCacheCtrl<SubstitutionCacheImpl> {

    public SubstitutionCacheCtrl() {
        super(SystemProperties.useStaleableSubstitutionCache() ? new StaleableSubstitutionCacheFactory() : new SubstitutionCacheFactory(),
                createListenObjectTypes());
    }

    /**
     * Returns for specified inactive {@link Actor} {@link Map} from substitution rule to {@link Set} of substitutors. If {@link Actor} is active then
     * empty result is returned.
     *
     * @param actor
     *            Actor, which substitution rules will be returned.
     * @param loadIfRequired
     *            Flag, equals true if substitution rules may be loaded from database if cache is empty and false to return null in this case.
     * @return {@link Map} from substitution rule to {@link Set} of substitutor id's.
     */
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, loadIfRequired);
    }

    /**
     * Try to get substitutors for actor. If cache is not initialized or substitutors not found this method will not query database - it returns null
     * instead.
     *
     * @param actor
     *            Actor, to get substitutors.
     * @return Substitutors for actor or null, if substitutors not initialized for actor.
     */
    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, false);
    }

    /**
     * Returns all inactive {@link Actor}'s, which has at least one substitution rule with specified actor as substitutor.
     *
     * @param actor
     *            {@link Actor}, which substituted actors will be returned.
     * @return All inactive {@link Actor} id's, which has at least one substitution rule with specified actor as substitutor.
     */
    public Set<Long> getSubstituted(Actor actor) {
        SubstitutionCacheImpl cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstituted(actor);
    }

    private static List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<>();
        result.add(new ListenObjectDefinition(Substitution.class));
        result.add(new ListenObjectDefinition(SubstitutionCriteria.class));
        result.add(new ListenObjectDefinition(ExecutorGroupMembership.class));
        result.add(new ListenObjectDefinition(Actor.class));
        result.add(new ListenObjectDefinition(Group.class));
        return result;
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
     * Static factory. It creates on the fly by demand and it state is always equals to database state. May leads to high delay if many executors and
     * substitutions is used. It's recommend to use {@link StaleableSubstitutionCacheFactory}.
     */
    private static class SubstitutionCacheFactory extends SMCacheFactory<SubstitutionCacheImpl> {

        SubstitutionCacheFactory() {
            super(Type.EAGER, null);
        }

        @Override
        protected SubstitutionCacheImpl createCacheImpl(CacheInitializationProcessContext context) {
            return new SubstitutionCacheImpl(true, false, null);
        }
    }

    /**
     * Non runtime factory. It creates on background and cache state may differs from database state for some time.
     */
    private static class StaleableSubstitutionCacheFactory extends SMCacheFactory<SubstitutionCacheImpl> {

        StaleableSubstitutionCacheFactory() {
            super(Type.LAZY_STALEABLE, new DefaultCacheTransactionalExecutor());
        }

        @Override
        protected SubstitutionCacheImpl createCacheStubImpl() {
            return new SubstitutionCacheImpl(false, true, null);
        }

        @Override
        protected SubstitutionCacheImpl createCacheImpl(CacheInitializationProcessContext context) {
            return new SubstitutionCacheImpl(true, true, context);
        }
    }
}
