package ru.runa.wfe.commons.cache.sm;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheStateFactory;
import ru.runa.wfe.commons.cache.states.IsolatedCacheStateFactory;
import ru.runa.wfe.commons.cache.states.staleable.StaleableCacheStateFactory;

public abstract class SMCacheFactory<CacheImpl extends CacheImplementation> {

    protected enum Type {

        /**
         * createCacheOrStub() returns real cache.
         * Subclasses must implement only createCacheImpl(); it will be called synchronously.
         */
        EAGER,

        /**
         * createCacheOrStub() returns stub, real cache is constructed in background by createCacheDelayed().
         * Subclasses must implement both createCacheStubImpl() and createCacheImpl(); the latter will be called asynchronously.
         */
        LAZY,

        /**
         * Same as LAZY, but old (stale) cache is returned instead of stub, if one exists.
         * That is, caches of other types are destroyed when entering EmptyCacheState (null is passed), but LAZY_STALEABLE cache is passed around.
         *
         * @see CacheStateFactory#createEmptyState(CacheImplementation)
         */
        LAZY_STALEABLE
    }

    protected final Log log = LogFactory.getLog(getClass());
    private final Type type;
    public final CacheStateFactory<CacheImpl> stateFactory;

    protected SMCacheFactory(Type type) {
        this.type = type;
        if (type == Type.LAZY_STALEABLE) {
            stateFactory = new StaleableCacheStateFactory<>();
        } else {
            stateFactory = new IsolatedCacheStateFactory<>();
        }
    }

    public final boolean isLazy() {
        return type != Type.EAGER;
    }

    public final CacheImpl createCacheOrStub() {
        return isLazy() ? createCacheStubImpl() : createCacheImpl(null);
    }

    public final void createCacheDelayed(CacheInitializationContext<CacheImpl> context) {
        Preconditions.checkState(isLazy());
        val thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    val cache = new AtomicReference<CacheImpl>();
                    if (!context.isInitializationStillRequired()) {
                        return;
                    }
                    getTransactionalExecutor().execute(() -> {
                        if (!context.isInitializationStillRequired()) {
                            return;
                        }
                        if (log.isTraceEnabled()) {
                            log.trace("Creating cache from " + this);
                        }
                        cache.set(createCacheImpl(context));
                        if (log.isDebugEnabled()) {
                            log.debug("Created cache " + cache.get());
                        }
                        context.onComplete(cache.get());
                    });
                } catch (Throwable e) {
                    log.error("", e);
                    context.onError(e);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Never called if type == EAGER.
     * @see Type
     */
    protected CacheImpl createCacheStubImpl() {
        throw new NotImplementedException();
    }

    /**
     * @param context Null if type == EAGER.
     * @see Type
     */
    protected abstract CacheImpl createCacheImpl(CacheInitializationProcessContext context);

    protected TransactionalExecutor getTransactionalExecutor() {
        return ApplicationContextFactory.getTransactionalExecutor();
    }
}
