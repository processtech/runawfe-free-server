package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Interface for audit cache state machine actions.
 */
public interface CacheStateMachineAudit<CacheImpl extends CacheImplementation> {

    /**
     * Get audit for GetCache operation.
     *
     * @return Return audit for GetCache operation.
     */
    GetCacheAudit<CacheImpl> auditGetCache();

    /**
     * Get audit for OnChange operation.
     *
     * @return Return audit for OnChange operation.
     */
    OnChangeAudit<CacheImpl> auditOnChange();

    /**
     * Get audit for CompleteTransaction operation.
     *
     * @return Return audit for CompleteTransaction operation.
     */
    CompleteTransactionAudit<CacheImpl> auditCompleteTransaction();

    /**
     * Get audit for BeforeTransactionComplete operation.
     *
     * @return Return audit for BeforeTransactionComplete operation.
     */
    BeforeTransactionCompleteAudit<CacheImpl> auditBeforeTransactionComplete();

    /**
     * Get audit for CommitCache operation.
     *
     * @return Return audit for CommitCache operation.
     */
    CommitCacheAudit<CacheImpl> auditCommitCache();

    /**
     * Get audit for InitializationError operation.
     *
     * @return Return audit for InitializationError operation.
     */
    InitializationErrorAudit<CacheImpl> auditInitializationError();

    /**
     * Get audit for Uninitialize operation.
     *
     * @return Return audit for Uninitialize operation.
     */
    StageSwitchAudit<CacheImpl> auditUninitialize();
}
