package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;

/**
 * Cache lifetime state machine. Current state is empty cache (initialization required).
 */
public class EmptyCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, DefaultStateContext> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(EmptyCacheState.class);

    private EmptyCacheState() {
    }

    @Override
    public boolean isDirtyTransactionExists() {
        return false;
    }

    @Override
    public boolean isDirtyTransaction(Transaction transaction) {
        return false;
    }

    @Override
    public CacheImpl getCacheQuickNoBuild(Transaction transaction) {
        return null;
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction) {
        return initiateCacheCreation(context);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return initiateCacheCreation(context);
    }

    /**
     * Create cache and start delayed initialization if required.
     *
     * @param context
     *            Cache state machine context with common used data.
     * @return Return next state for state machine.
     */
    private StateCommandResultWithCache<CacheImpl, DefaultStateContext> initiateCacheCreation(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context) {
        CacheImpl cache = context.getCacheFactory().createCache();
        if (context.getCacheFactory().hasDelayedInitialization()) {
            CacheState<CacheImpl, DefaultStateContext> initializingState = context.getStateFactory().createInitializingState(cache, null);
            return StateCommandResultWithCache.create(initializingState, cache);
        }
        cache.commitCache();
        return StateCommandResultWithCache.create(context.getStateFactory().createInitializedState(cache, null), cache);
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> onChange(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(null, dirtyTransaction, null));
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean, DefaultStateContext> completeTransaction(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
        return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null, null), true);
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> commitCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl, DefaultStateContext> context) {
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> dropCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context) {
        return StateCommandResult.create(context.getStateFactory().createEmptyState(null, null));
    }

    /**
     * Create empty state for state machine.
     *
     * @return Return empty state.
     */
    public static <CacheImpl extends CacheImplementation> EmptyCacheState<CacheImpl> createEmptyState() {
        return new EmptyCacheState<CacheImpl>();
    }
}
