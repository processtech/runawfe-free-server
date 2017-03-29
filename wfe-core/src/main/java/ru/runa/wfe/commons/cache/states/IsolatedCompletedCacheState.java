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
public class IsolatedCompletedCacheState<CacheImpl extends CacheImplementation> implements CacheState<CacheImpl, DefaultStateContext> {

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
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCache(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResultWithCache<CacheImpl, DefaultStateContext> getCacheIfNotLocked(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        return StateCommandResultWithCache.createNoStateSwitch(cache);
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> onChange(CacheStateMachineContext<CacheImpl, DefaultStateContext> context,
            Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(context.getStateFactory().createDirtyState(cache, dirtyTransaction, null));
    }

    @Override
    public StateCommandResult<CacheImpl, DefaultStateContext> beforeTransactionComplete(
            CacheStateMachineContext<CacheImpl, DefaultStateContext> context, Transaction transaction) {
        log.error("beforeTransactionComplete must not be called on " + this);
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
}
