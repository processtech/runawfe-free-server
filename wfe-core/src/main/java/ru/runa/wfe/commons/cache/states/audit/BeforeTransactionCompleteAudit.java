package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface BeforeTransactionCompleteAudit<CacheImpl extends CacheImplementation, StateContext>
        extends StageSwitchAudit<CacheImpl, StateContext> {

}
