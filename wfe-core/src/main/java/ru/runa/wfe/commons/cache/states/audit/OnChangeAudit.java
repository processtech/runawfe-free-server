package ru.runa.wfe.commons.cache.states.audit;

import javax.transaction.Transaction;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

public interface OnChangeAudit<CacheImpl extends CacheImplementation, StateContext> extends StageSwitchAudit<CacheImpl, StateContext> {

    void beforeOnChange(Transaction transaction, ChangedObjectParameter changedObject);

    void afterOnChange(Transaction transaction, ChangedObjectParameter changedObject);

}