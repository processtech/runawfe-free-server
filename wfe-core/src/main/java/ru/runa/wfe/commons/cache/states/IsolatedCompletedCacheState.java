package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;

/**
 * Cache lifetime state machine. Current state is fully operational cache (cache is initialized).
 */
public class IsolatedCompletedCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl> {

    /**
     * Logging support.
     */
    private static Log log = LogFactory.getLog(EmptyCacheState.class);

    /**
     * Current cache instance.
     */
    private final CacheImpl cache;

    public IsolatedCompletedCacheState(CacheImpl cache) {
        this.cache = cache;
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
        return cache;
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCache(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return new StateCommandResultWithCache<CacheImpl>(null, cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        return new StateCommandResultWithCache<CacheImpl>(null, cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(CacheStateMachineContext<CacheImpl> context, Transaction transaction,
            ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return new StateCommandResult<CacheImpl>(context.getStateFactory().createDirtyState(cache, dirtyTransaction, null));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        log.error("beforeTransactionComplete must not be called on " + this);
        return StateCommandResult.stateNoChangedResult;
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(CacheStateMachineContext<CacheImpl> context, Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
        return new StateCommandResultWithData<CacheImpl, Boolean>(context.getStateFactory().createEmptyState(null, null), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheStateMachineContext<CacheImpl> context, CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.stateNoChangedResult;
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept(CacheStateMachineContext<CacheImpl> context) {
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache(CacheStateMachineContext<CacheImpl> context) {
        return new StateCommandResult<CacheImpl>(context.getStateFactory().createEmptyState(null, null));
    }
}
