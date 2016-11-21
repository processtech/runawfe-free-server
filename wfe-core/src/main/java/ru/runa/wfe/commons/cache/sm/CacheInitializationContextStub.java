package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Lazy cache initialization context (stub - always do full initialization).
 */
public class CacheInitializationContextStub<CacheImpl extends CacheImplementation> implements CacheInitializationContext<CacheImpl> {

    @Override
    public boolean isInitializationStillRequired() {
        return true;
    }

    @Override
    public void onComplete(CacheImpl initializedCache) {
    }

    @Override
    public void onError(Throwable e) {
    }
}
