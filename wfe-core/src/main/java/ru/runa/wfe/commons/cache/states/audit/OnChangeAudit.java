package ru.runa.wfe.commons.cache.states.audit;

import javax.transaction.Transaction;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

public interface OnChangeAudit<CacheImpl extends CacheImplementation> extends StageSwitchAudit<CacheImpl> {

    void beforeOnChange(Transaction transaction, ChangedObjectParameter changedObject);

    void afterOnChange(Transaction transaction, ChangedObjectParameter changedObject);

}