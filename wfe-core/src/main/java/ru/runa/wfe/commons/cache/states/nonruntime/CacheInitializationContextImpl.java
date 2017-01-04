package ru.runa.wfe.commons.cache.states.nonruntime;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.CacheInitializationCallback;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;

/**
 * Lazy cache initialization context.
 */
public class CacheInitializationContextImpl<CacheImpl extends CacheImplementation> implements CacheInitializationContext<CacheImpl> {
    /**
     * Cache state machine state, which starts lazy initialization.
     */
    private final CacheInitializingState<CacheImpl> state;

    /**
     * Callback object to receive notification on lazy initialization complete.
     */
    private final CacheInitializationCallback<CacheImpl> callback;

    public CacheInitializationContextImpl(CacheInitializingState<CacheImpl> state, CacheInitializationCallback<CacheImpl> callback) {
        this.state = state;
        this.callback = callback;
    }

    @Override
    public boolean isInitializationStillRequired() {
        return state.isInitializationStillRequired();
    }

    @Override
    public void onComplete(CacheImpl initializedCache) {
        if (isInitializationStillRequired()) {
            callback.commitCache(state, initializedCache);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (isInitializationStillRequired()) {
            callback.onError(state, e);
        }
    }
}
