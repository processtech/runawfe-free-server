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
    public GetCacheAudit<CacheImpl> auditGetCache();

    /**
     * Get audit for OnChange operation.
     * 
     * @return Return audit for OnChange operation.
     */
    public OnChangeAudit<CacheImpl> auditOnChange();

    /**
     * Get audit for CompleteTransaction operation.
     * 
     * @return Return audit for CompleteTransaction operation.
     */
    public CompleteTransactionAudit<CacheImpl> auditCompleteTransaction();

    /**
     * Get audit for BeforeTransactionComplete operation.
     * 
     * @return Return audit for BeforeTransactionComplete operation.
     */
    public BeforeTransactionCompleteAudit<CacheImpl> auditBeforeTransactionComplete();

    /**
     * Get audit for CommitCache operation.
     * 
     * @return Return audit for CommitCache operation.
     */
    public CommitCacheAudit<CacheImpl> auditCommitCache();

    /**
     * Get audit for InitializationError operation.
     * 
     * @return Return audit for InitializationError operation.
     */
    public InitializationErrorAudit<CacheImpl> auditInitializationError();

    /**
     * Get audit for Uninitialize operation.
     * 
     * @return Return audit for Uninitialize operation.
     */
    public StageSwitchAudit<CacheImpl> auditUninitialize();
}
