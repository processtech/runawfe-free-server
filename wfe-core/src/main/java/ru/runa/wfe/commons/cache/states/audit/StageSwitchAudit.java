package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheState;

public interface StageSwitchAudit<CacheImpl extends CacheImplementation, StateContext> {
    public void stayStage();

    public void stageSwitched(CacheState<CacheImpl, StateContext> from, CacheState<CacheImpl, StateContext> to);

    public void stageSwitchFailed(CacheState<CacheImpl, StateContext> from, CacheState<CacheImpl, StateContext> to);

    public void nextStageFatalError();
}
