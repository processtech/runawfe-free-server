package ru.runa.wfe.commons.cache.states.audit;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.states.CacheState;

/**
 * Default implementation for {@link CacheStateMachineAudit}. Do nothing.
 *
 * @param <CacheImpl>
 *            Cache implementation.
 */
public class DefaultCacheStateMachineAudit<CacheImpl extends CacheImplementation> implements CacheStateMachineAudit<CacheImpl> {

    private final GetCacheAudit<CacheImpl> auditGetCache = new DefaultGetCacheAudit();

    private final OnChangeAudit<CacheImpl> auditOnChange = new DefaultOnChangeAudit();

    private final CompleteTransactionAudit<CacheImpl> auditCompleteTransaction = new DefaultCompleteTransactionAudit();

    private final CommitCacheAudit<CacheImpl> auditCommitCache = new DefaultCommitCacheAudit();

    private final InitializationErrorAudit<CacheImpl> auditInitializationError = new DefaultInitializationErrorAudit();

    private final StageSwitchAudit<CacheImpl> auditDropCache = new DefaultStageSwitchAudit();

    private final BeforeTransactionCompleteAudit<CacheImpl> auditBeforeTransactionCommit = new DefaultBeforeTransactionCompleteAudit();

    @Override
    public GetCacheAudit<CacheImpl> auditGetCache() {
        return auditGetCache;
    }

    @Override
    public OnChangeAudit<CacheImpl> auditOnChange() {
        return auditOnChange;
    }

    @Override
    public CompleteTransactionAudit<CacheImpl> auditCompleteTransaction() {
        return auditCompleteTransaction;
    }

    @Override
    public CommitCacheAudit<CacheImpl> auditCommitCache() {
        return auditCommitCache;
    }

    @Override
    public InitializationErrorAudit<CacheImpl> auditInitializationError() {
        return auditInitializationError;
    }

    @Override
    public StageSwitchAudit<CacheImpl> auditDropCache() {
        return auditDropCache;
    }

    @Override
    public BeforeTransactionCompleteAudit<CacheImpl> auditBeforeTransactionComplete() {
        return auditBeforeTransactionCommit;
    }

    class DefaultStageSwitchAudit implements StageSwitchAudit<CacheImpl> {

        @Override
        public void stayStage() {
        }

        @Override
        public void stageSwitched(CacheState<CacheImpl> from, CacheState<CacheImpl> to) {
        }

        @Override
        public void stageSwitchFailed(CacheState<CacheImpl> from, CacheState<CacheImpl> to) {
        }

        @Override
        public void nextStageFatalError() {
        }
    }

    class DefaultGetCacheAudit extends DefaultStageSwitchAudit implements GetCacheAudit<CacheImpl> {

        @Override
        public void quickResult(Transaction transaction, CacheImpl cache) {
        }

        @Override
        public void beforeCreation(Transaction transaction) {
        }

        @Override
        public void afterCreation(Transaction transaction, CacheImpl cache) {
        }
    }

    class DefaultOnChangeAudit extends DefaultStageSwitchAudit implements OnChangeAudit<CacheImpl> {

        @Override
        public void beforeOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
        }

        @Override
        public void afterOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
        }
    }

    class DefaultCompleteTransactionAudit extends DefaultStageSwitchAudit implements CompleteTransactionAudit<CacheImpl> {

        @Override
        public void beforeCompleteTransaction(Transaction transaction) {
        }

        @Override
        public void afterCompleteTransaction(Transaction transaction) {
        }

        @Override
        public void allTransactionsCompleted(Transaction transaction) {
        }
    }

    class DefaultCommitCacheAudit extends DefaultStageSwitchAudit implements CommitCacheAudit<CacheImpl> {

        @Override
        public void stageIsNotCommitStage(CacheImpl cache) {
        }

        @Override
        public void beforeCommit(CacheImpl cache) {
        }

        @Override
        public void afterCommit(CacheImpl cache) {
        }
    }

    class DefaultInitializationErrorAudit extends DefaultStageSwitchAudit implements InitializationErrorAudit<CacheImpl> {

        @Override
        public void onInitializationError(Throwable e) {
        }
    }

    class DefaultBeforeTransactionCompleteAudit extends DefaultStageSwitchAudit implements BeforeTransactionCompleteAudit<CacheImpl> {
    }
}
