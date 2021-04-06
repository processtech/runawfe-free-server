package ru.runa.wfe.commons.cache.states.audit;

import javax.transaction.Transaction;
import ru.runa.wfe.commons.cache.CacheImplementation;

public interface GetCacheAudit<CacheImpl extends CacheImplementation> extends StageSwitchAudit<CacheImpl> {

    void quickResult(Transaction transaction, CacheImpl cache);

    void beforeCreation(Transaction transaction);

    void afterCreation(Transaction transaction, CacheImpl cache);

}