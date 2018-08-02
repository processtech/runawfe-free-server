package ru.runa.wfe.commons.cache.sm.factories;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.CacheTransactionalExecutor;
import ru.runa.wfe.commons.cache.sm.SMCacheFactory;

/**
 * Cache factory for static caches. These caches block all thread's execution before cache initialization complete.
 *
 * TODO Get rid of. Currently needed because of overloaded CacheStateMachine.createStateMachine() methods having different logic.
 */
public abstract class StaticCacheFactory<CacheImpl extends CacheImplementation> extends SMCacheFactory<CacheImpl> {

    protected StaticCacheFactory(boolean hasDelayedInitialization, CacheTransactionalExecutor transactionalExecutor) {
        super(hasDelayedInitialization, transactionalExecutor);
    }
}
