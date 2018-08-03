package ru.runa.wfe.commons.cache.sm;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheStateFactory;
import ru.runa.wfe.commons.cache.states.DefaultCacheStateFactory;
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
    public final Type type;
    public final CacheStateFactory<CacheImpl> stateFactory;
    private final CacheTransactionalExecutor transactionalExecutor;

    /**
     * @param isolated  Used by tests; other callers should use another constructor overload.
     */
    protected SMCacheFactory(Type type, boolean isolated, CacheTransactionalExecutor transactionalExecutor) {
        Preconditions.checkArgument(type == Type.EAGER || transactionalExecutor != null);
        this.type = type;
        if (type == Type.LAZY_STALEABLE) {
            stateFactory = new StaleableCacheStateFactory<>();
        } else if (isolated) {
            stateFactory = new IsolatedCacheStateFactory<>();
        } else {
            stateFactory = new DefaultCacheStateFactory<>();
        }
        this.transactionalExecutor = transactionalExecutor;
    }

    protected SMCacheFactory(Type type, CacheTransactionalExecutor transactionalExecutor) {
        this(type, SystemProperties.useIsolatedCacheStateMachine(), transactionalExecutor);
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
                    transactionalExecutor.executeInTransaction(new Runnable() {

                        @Override
                        public void run() {
                            if (!context.isInitializationStillRequired()) {
                                return;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("Creating cache from " + this);
                            }
                            cache.set(createCacheImpl(context));
                            if (log.isDebugEnabled()) {
                                log.debug("Created cache from " + this + ": " + cache.get());
                            }
                            context.onComplete(cache.get());
                        }
                    });
                } catch (Throwable e) {
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
}
