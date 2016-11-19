package ru.runa.wfe.commons.cache.states;

import javax.transaction.Transaction;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.sm.CacheStateMachineContext;

/**
 * Interface for every state of cache lifetime state machine.
 */
public interface CacheState<CacheImpl extends CacheImplementation> {

    /**
     * Check if dirty transactions exists for cache.
     * 
     * @return Return true, if dirty transaction exists and false otherwise.
     */
    boolean isDirtyTransactionExists();

    /**
     * Check if transaction is dirty for cache.
     * 
     * @param transaction
     *            Transaction to check.
     * @return Return true, if transaction is dirty and false otherwise.
     */
    boolean isDirtyTransaction(Transaction transaction);

    /**
     * Fast attempt to get cache. State may return cache only if it already created. No building is allowed.
     * 
     * @param transaction
     *            Transaction, which requested cache.
     * 
     * @return Returns already builded cache or null, if no cache is builded yet.
     */
    CacheImpl getCacheQuickNoBuild(Transaction transaction);

    /**
     * Called to get cache. Cache must be created in all case.
     * 
     * @param context
     *            Cache state machine context with common used data.
     * @param transaction
     *            Transaction, which requested cache.
     * @return Returns next state and cache. Next state may be null if no state change is required.
     */
    StateCommandResultWithCache<CacheImpl> getCache(CacheStateMachineContext<CacheImpl> context, Transaction transaction);

    /**
     * Called to get cache. Must returns null (or already created cache) if cache is locked (has dirty transactions): no cache creation is allowed
     * (return as fast as possible). If cache is not locked, then creates cache.
     * 
     * @param context
     *            Cache state machine context with common used data.
     * @param transaction
     *            Transaction, which requested cache.
     * @return Returns next state and cache. Next state may be null if no state change is required. Cache may be null, if cache is locked.
     */
    StateCommandResultWithCache<CacheImpl> getCacheIfNotLocked(CacheStateMachineContext<CacheImpl> context, Transaction transaction);

    /**
     * Notification about changed object. This method MUST return new state; if new state not created then we have a rise condition: starting
     * initialize process after not completed changing transaction.
     * 
     * @param context
     *            Cache state machine context with common used data.
     * @param transaction
     *            Transaction, which change persistent object.
     * @param changedObject
     *            Changed object description.
     * @return Returns next state is mandatory.
     */
    StateCommandResult<CacheImpl> onChange(CacheStateMachineContext<CacheImpl> context, Transaction transaction, ChangedObjectParameter changedObject);

    /**
     * Notifies about prepare to transaction completion.
     * 
     * @param context
     *            Cache state machine context with common used data.
     * @param transaction
     *            Transaction, which will be completed.
     * @return Returns next state. Next state may be null if no state change is required.
     */
    StateCommandResult<CacheImpl> beforeTransactionComplete(CacheStateMachineContext<CacheImpl> context, Transaction transaction);

    /**
     * Notifies cache about transaction completion. This method MUST return new state. if new state not created then we have a rise condition:
     * changing transaction may switch to new state, where current transaction is not marked as completed.
     * 
     * @param context
     *            Cache state machine context with common used data.
     * @param transaction
     *            Completed transaction (committed or rollbacked).
     * @return Returns next state and all dirty transaction reset flag. Flag equals true, if no dirty transaction left and false otherwise.
     */
    StateCommandResultWithData<CacheImpl, Boolean> completeTransaction(CacheStateMachineContext<CacheImpl> context, Transaction transaction);

    /**
     * Commits (accept) initialized cache.
     * 
     * @param context
     *            Cache state machine context with common used data.
     * @param cache
     *            Initialized cache to commit (accept).
     * @return Returns next state. Next state may be null if no state change is required.
     */
    StateCommandResult<CacheImpl> commitCache(CacheStateMachineContext<CacheImpl> context, CacheImpl cache);

    /**
     * Discard this state. All lazy work must not be done - this state and caches from it will not be used.
     */
    void discard();

    /**
     * Accept this state. Called then state is accepted by state machine. Delayed initialization may be started.
     * 
     * @param context
     *            Cache state machine context with common used data.
     */
    void accept(CacheStateMachineContext<CacheImpl> context);

    /**
     * Called to drop cache instance. It must be changed to empty.
     * 
     * @param context
     *            Cache state machine context with common used data.
     */
    StateCommandResult<CacheImpl> dropCache(CacheStateMachineContext<CacheImpl> context);
}
