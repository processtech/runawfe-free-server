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
public class EmptyCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(EmptyCacheState.class);

    public EmptyCacheState() {
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
    public StateCommandResultWithCache<CacheImpl> getCache(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction
    ) {
        return initiateCacheCreation(context);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction
    ) {
        return initiateCacheCreation(context);
    }

    /**
     * Create cache and start delayed initialization if required.
     *
     * @param context
     *            Cache state machine context with common used data.
     * @return Return next state for state machine.
     */
    private StateCommandResultWithCache<CacheImpl> initiateCacheCreation(CacheStateMachineContext<CacheImpl> context) {
        CacheImpl cache = context.getCacheFactory().createCache();
        if (context.getCacheFactory().hasDelayedInitialization()) {
            CacheState<CacheImpl> initializingState = context.getStateFactory().createInitializingState(cache);
            return StateCommandResultWithCache.create(initializingState, cache);
        }
        cache.commitCache();
        return StateCommandResultWithCache.create(context.getStateFactory().createInitializedState(cache), cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction, ChangedObjectParameter changedObject
    ) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(null, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl> context, Transaction transaction
    ) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
        return StateCommandResultWithData.create(context.getStateFactory().createEmptyState(null), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheStateMachineContext<CacheImpl> context, CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl> context) {
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache(CacheStateMachineContext<CacheImpl> context) {
        return StateCommandResult.create(context.getStateFactory().createEmptyState(null));
    }
}
