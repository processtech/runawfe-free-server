package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Cache lifetime state machine. Current state is empty cache (initialization required).
 */
@CommonsLog
public class EmptyCacheState<CacheImpl extends CacheImplementation> extends CacheState<CacheImpl> {

    public EmptyCacheState(CacheStateMachine<CacheImpl> owner) {
        super(owner);
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
    public StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction) {
        return initiateCacheCreation();
    }

    @Override
    public StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction) {
        return initiateCacheCreation();
    }

    /**
     * Create cache and start delayed initialization if required.
     *
     * @return Return next state for state machine.
     */
    private StateCommandResultWithCache<CacheImpl> initiateCacheCreation() {
        CacheImpl cache = getCacheFactory().createCache();
        if (getCacheFactory().hasDelayedInitialization()) {
            CacheState<CacheImpl> initializingState = getStateFactory().createInitializingState(cache);
            return StateCommandResultWithCache.create(initializingState, cache);
        }
        cache.commitCache();
        return StateCommandResultWithCache.create(getStateFactory().createInitializedState(cache), cache);
    }

    @Override
    public StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        DirtyTransactions<CacheImpl> dirtyTransaction = DirtyTransactions.createOneDirtyTransaction(transaction, null);
        return StateCommandResult.create(getStateFactory().createDirtyState(null, dirtyTransaction));
    }

    @Override
    public StateCommandResult<CacheImpl> beforeTransactionComplete(Transaction transaction) {
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(Transaction transaction) {
        log.error("completeTransaction must not be called on " + this);
        return StateCommandResultWithData.create(getStateFactory().createEmptyState(null), true);
    }

    @Override
    public StateCommandResult<CacheImpl> commitCache(CacheImpl cache) {
        log.error("commitCache must not be called on " + this);
        return StateCommandResult.createNoStateSwitch();
    }

    @Override
    public void discard() {
    }

    @Override
    public void accept() {
    }

    @Override
    public StateCommandResult<CacheImpl> dropCache() {
        return StateCommandResult.create(getStateFactory().createEmptyState(null));
    }
}
