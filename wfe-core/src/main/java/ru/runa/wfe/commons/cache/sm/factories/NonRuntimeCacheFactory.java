package ru.runa.wfe.commons.cache.sm.factories;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.CacheTransactionalExecutor;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;

/**
 * Cache factory for non runtime caches. It may return not actual data after data change for some time.
 *
 * TODO Get rid of. Currently needed because of overloaded CacheStateMachine.createStateMachine() methods having different logic.
 */
public abstract class NonRuntimeCacheFactory<CacheImpl extends CacheImplementation> extends SMCacheFactory<CacheImpl> {

    protected NonRuntimeCacheFactory(boolean hasDelayedInitialization, CacheTransactionalExecutor transactionalExecutor) {
        super(hasDelayedInitialization, transactionalExecutor);
    }
}
