package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheState;

public interface StageSwitchAudit<CacheImpl extends CacheImplementation> {
    public void stayStage();

    public void stageSwitched(CacheState<CacheImpl> from, CacheState<CacheImpl> to);

    public void stageSwitchFailed(CacheState<CacheImpl> from, CacheState<CacheImpl> to);

    public void nextStageFatalError();
}
