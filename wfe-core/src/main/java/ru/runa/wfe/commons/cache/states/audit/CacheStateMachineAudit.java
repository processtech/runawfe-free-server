package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Interface for audit cache state machine actions.
 */
public interface CacheStateMachineAudit<CacheImpl extends CacheImplementation, StateContext> {

    /**
     * Get audit for GetCache operation.
     *
     * @return Return audit for GetCache operation.
     */
    public GetCacheAudit<CacheImpl, StateContext> auditGetCache();

    /**
     * Get audit for OnChange operation.
     *
     * @return Return audit for OnChange operation.
     */
    public OnChangeAudit<CacheImpl, StateContext> auditOnChange();

    /**
     * Get audit for CompleteTransaction operation.
     *
     * @return Return audit for CompleteTransaction operation.
     */
    public CompleteTransactionAudit<CacheImpl, StateContext> auditCompleteTransaction();

    /**
     * Get audit for BeforeTransactionComplete operation.
     *
     * @return Return audit for BeforeTransactionComplete operation.
     */
    public BeforeTransactionCompleteAudit<CacheImpl, StateContext> auditBeforeTransactionComplete();

    /**
     * Get audit for CommitCache operation.
     *
     * @return Return audit for CommitCache operation.
     */
    public CommitCacheAudit<CacheImpl, StateContext> auditCommitCache();

    /**
     * Get audit for InitializationError operation.
     *
     * @return Return audit for InitializationError operation.
     */
    public InitializationErrorAudit<CacheImpl, StateContext> auditInitializationError();

    /**
     * Get audit for Uninitialize operation.
     *
     * @return Return audit for Uninitialize operation.
     */
    public StageSwitchAudit<CacheImpl, StateContext> auditUninitialize();
}
