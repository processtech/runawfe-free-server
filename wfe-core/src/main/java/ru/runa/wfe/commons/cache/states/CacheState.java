package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;
import lombok.NonNull;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheFactory;
import ru.runa.wfe.commons.cache.sm.CacheInitializationCallback;
import ru.runa.wfe.commons.cache.sm.CacheStateMachine;

/**
 * Interface for every state of cache lifetime state machine.
 */
public abstract class CacheState<CacheImpl extends CacheImplementation> {

    protected final CacheStateMachine<CacheImpl> owner;

    public CacheState(@NonNull CacheStateMachine<CacheImpl> owner) {
        this.owner = owner;
    }

    protected final CacheInitializationCallback<CacheImpl> getInitializationCallback() {
        return owner;
    }

    protected final CacheFactory<CacheImpl> getCacheFactory() {
        return owner.getCacheFactory();
    }

    protected final CacheStateFactory<CacheImpl> getStateFactory() {
        return owner.getStateFactory();
    }


    /**
     * Check if dirty transactions exists for cache.
     *
     * @return Return true, if dirty transaction exists and false otherwise.
     */
    public abstract boolean isDirtyTransactionExists();

    /**
     * Check if transaction is dirty for cache.
     *
     * @param transaction
     *            Transaction to check.
     * @return Return true, if transaction is dirty and false otherwise.
     */
    public abstract boolean isDirtyTransaction(Transaction transaction);

    /**
     * Fast attempt to get cache. State may return cache only if it already created. No building is allowed.
     *
     * @param transaction
     *            Transaction, which requested cache.
     *
     * @return Returns already builded cache or null, if no cache is builded yet.
     */
    public abstract CacheImpl getCacheQuickNoBuild(Transaction transaction);

    /**
     * Called to get cache. Cache must be created in all case.
     *
     * @param transaction
     *            Transaction, which requested cache.
     * @return Returns next state and cache. Next state may be null if no state change is required.
     */
    public abstract StateCommandResultWithCache<CacheImpl> getCache(Transaction transaction);

    /**
     * Called to get cache. Must returns null (or already created cache) if cache is locked (has dirty transactions): no cache creation is allowed
     * (return as fast as possible). If cache is not locked, then creates cache.
     *
     * @param transaction
     *            Transaction, which requested cache.
     * @return Returns next state and cache. Next state may be null if no state change is required. Cache may be null, if cache is locked.
     */
    public abstract StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(Transaction transaction);

    /**
     * Notification about changed object. This method MUST return new state; if new state not created then we have a rise condition: starting
     * initialize process after not completed changing transaction.
     *
     * @param transaction
     *            Transaction, which change persistent object.
     * @param changedObject
     *            Changed object description.
     * @return Returns next state is mandatory.
     */
    public abstract StateCommandResult<CacheImpl> onChange(Transaction transaction, ChangedObjectParameter changedObject);

    /**
     * Notifies about prepare to transaction completion.
     *
     * @param transaction
     *            Transaction, which will be completed.
     * @return Returns next state. Next state may be null if no state change is required.
     */
    public abstract StateCommandResult<CacheImpl> beforeTransactionComplete(Transaction transaction);

    /**
     * Notifies cache about transaction completion. This method MUST return new state. if new state not created then we have a rise condition:
     * changing transaction may switch to new state, where current transaction is not marked as completed.
     *
     * @param transaction
     *            Completed transaction (committed or rollbacked).
     * @return Returns next state and all dirty transaction reset flag. Flag equals true, if no dirty transaction left and false otherwise.
     */
    public abstract StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(Transaction transaction);

    /**
     * Commits (accept) initialized cache.
     *
     * @param cache
     *            Initialized cache to commit (accept).
     * @return Returns next state. Next state may be null if no state change is required.
     */
    public abstract StateCommandResult<CacheImpl> commitCache(CacheImpl cache);

    /**
     * Discard this state. All lazy work must not be done - this state and caches from it will not be used.
     */
    public abstract void discard();

    /**
     * Accept this state. Called then state is accepted by state machine. Delayed initialization may be started.
     */
    public abstract void accept();

    /**
     * Called to drop cache instance. It must be changed to empty.
     */
    public abstract StateCommandResult<CacheImpl> dropCache();
}
