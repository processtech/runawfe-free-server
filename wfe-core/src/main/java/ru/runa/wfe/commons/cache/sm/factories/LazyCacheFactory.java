package ru.runa.wfe.commons.cache.sm.factories;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;
import ru.runa.wfe.commons.cache.sm.CacheTransactionalExecutor;

/**
 * Cache factory for creating lazily initialized caches. These caches returns some proxy implementation before initialization will be completed.
 *
 * TODO Get rid of. Currently needed because of overloaded CacheStateMachine.createStateMachine() methods having different logic.
 */
public abstract class LazyCacheFactory<CacheImpl extends CacheImplementation> extends SMCacheFactory<CacheImpl> {

    protected LazyCacheFactory(boolean hasDelayedInitialization, CacheTransactionalExecutor transactionalExecutor) {
        super(hasDelayedInitialization, transactionalExecutor);
    }
}
