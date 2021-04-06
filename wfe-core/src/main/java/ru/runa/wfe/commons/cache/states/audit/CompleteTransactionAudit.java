package ru.runa.wfe.commons.cache.states.audit;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;

public interface CompleteTransactionAudit<CacheImpl extends CacheImplementation> extends StageSwitchAudit<CacheImpl> {

    void beforeCompleteTransaction(Transaction transaction);

    void afterCompleteTransaction(Transaction transaction);

    void allTransactionsCompleted(Transaction transaction);

}