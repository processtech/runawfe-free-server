package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface CommitCacheAudit<CacheImpl extends CacheImplementation> extends StageSwitchAudit<CacheImpl> {
    public void stageIsNotCommitStage(CacheImpl cache);

    public void beforeCommit(CacheImpl cache);

    public void afterCommit(CacheImpl cache);
}