package ru.runa.wfe.commons.cache.states.audit;

import javax.transaction.Transaction;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface CompleteTransactionAudit<CacheImpl extends CacheImplementation, StateContext> extends StageSwitchAudit<CacheImpl, StateContext> {

    void beforeCompleteTransaction(Transaction transaction);

    void afterCompleteTransaction(Transaction transaction);

    void allTransactionsCompleted(Transaction transaction);

}