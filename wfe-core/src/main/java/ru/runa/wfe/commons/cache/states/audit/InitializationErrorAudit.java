package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface InitializationErrorAudit<CacheImpl extends CacheImplementation, StateContext> extends StageSwitchAudit<CacheImpl, StateContext> {

    void onInitializationError(Throwable e);

}
