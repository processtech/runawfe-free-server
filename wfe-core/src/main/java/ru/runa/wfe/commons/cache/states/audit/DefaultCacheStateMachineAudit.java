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
public class DefaultCacheStateMachineAudit<CacheImpl extends CacheImplementation, StateContext>
        implements CacheStateMachineAudit<CacheImpl, StateContext> {

    private final GetCacheAudit<CacheImpl, StateContext> auditGetCache = new DefaultGetCacheAudit();

    private final OnChangeAudit<CacheImpl, StateContext> auditOnChange = new DefaultOnChangeAudit();

    private final CompleteTransactionAudit<CacheImpl, StateContext> auditCompleteTransaction = new DefaultCompleteTransactionAudit();

    private final CommitCacheAudit<CacheImpl, StateContext> auditCommitCache = new DefaultCommitCacheAudit();

    private final InitializationErrorAudit<CacheImpl, StateContext> auditInitializationError = new DefaultInitializationErrorAudit();

    private final StageSwitchAudit<CacheImpl, StateContext> auditUninitialize = new DefaultStageSwitchAudit();

    private final BeforeTransactionCompleteAudit<CacheImpl, StateContext> auditBeforeTransactionCommit = new DefaultBeforeTransactionCompleteAudit();

    @Override
    public GetCacheAudit<CacheImpl, StateContext> auditGetCache() {
        return auditGetCache;
    }

    @Override
    public OnChangeAudit<CacheImpl, StateContext> auditOnChange() {
        return auditOnChange;
    }

    @Override
    public CompleteTransactionAudit<CacheImpl, StateContext> auditCompleteTransaction() {
        return auditCompleteTransaction;
    }

    @Override
    public CommitCacheAudit<CacheImpl, StateContext> auditCommitCache() {
        return auditCommitCache;
    }

    @Override
    public InitializationErrorAudit<CacheImpl, StateContext> auditInitializationError() {
        return auditInitializationError;
    }

    @Override
    public StageSwitchAudit<CacheImpl, StateContext> auditUninitialize() {
        return auditUninitialize;
    }

    @Override
    public BeforeTransactionCompleteAudit<CacheImpl, StateContext> auditBeforeTransactionComplete() {
        return auditBeforeTransactionCommit;
    }

    class DefaultStageSwitchAudit implements StageSwitchAudit<CacheImpl, StateContext> {

        @Override
        public void stayStage() {
        }

        @Override
        public void stageSwitched(CacheState<CacheImpl, StateContext> from, CacheState<CacheImpl, StateContext> to) {
        }

        @Override
        public void stageSwitchFailed(CacheState<CacheImpl, StateContext> from, CacheState<CacheImpl, StateContext> to) {
        }

        @Override
        public void nextStageFatalError() {
        }
    }

    class DefaultGetCacheAudit extends DefaultStageSwitchAudit implements GetCacheAudit<CacheImpl, StateContext> {

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

    class DefaultOnChangeAudit extends DefaultStageSwitchAudit implements OnChangeAudit<CacheImpl, StateContext> {

        @Override
        public void beforeOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
        }

        @Override
        public void afterOnChange(Transaction transaction, ChangedObjectParameter changedObject) {
        }
    }

    class DefaultCompleteTransactionAudit extends DefaultStageSwitchAudit implements CompleteTransactionAudit<CacheImpl, StateContext> {

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

    class DefaultCommitCacheAudit extends DefaultStageSwitchAudit implements CommitCacheAudit<CacheImpl, StateContext> {

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

    class DefaultInitializationErrorAudit extends DefaultStageSwitchAudit implements InitializationErrorAudit<CacheImpl, StateContext> {

        @Override
        public void onInitializationError(Throwable e) {
        }
    }

    class DefaultBeforeTransactionCompleteAudit extends DefaultStageSwitchAudit implements BeforeTransactionCompleteAudit<CacheImpl, StateContext> {
    }
}
