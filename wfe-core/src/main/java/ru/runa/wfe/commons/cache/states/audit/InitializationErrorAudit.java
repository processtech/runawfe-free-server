package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface InitializationErrorAudit<CacheImpl extends CacheImplementation> extends StageSwitchAudit<CacheImpl> {

    void onInitializationError(Throwable e);

}
