package ru.runa.wfe.commons.cache.states.audit;

import ru.runa.wfe.commons.cache.CacheImplementation;

public interface CommitCacheAudit<CacheImpl extends CacheImplementation> extends StageSwitchAudit<CacheImpl> {

    void stageIsNotCommitStage(CacheImpl cache);

    void beforeCommit(CacheImpl cache);

    void afterCommit(CacheImpl cache);
}