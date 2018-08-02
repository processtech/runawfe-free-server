package ru.runa.wfe.commons.cache.sm;

import com.google.common.base.Preconditions;
import java.util.concurrent.atomic.AtomicReference;
import lombok.val;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.cache.CacheImplementation;

public abstract class SMCacheFactory<CacheImpl extends CacheImplementation> {
    protected final Log log = LogFactory.getLog(getClass());

    public final boolean hasDelayedInitialization;

    private final CacheTransactionalExecutor transactionalExecutor;

    protected SMCacheFactory(boolean hasDelayedInitialization, CacheTransactionalExecutor transactionalExecutor) {
        Preconditions.checkArgument(!hasDelayedInitialization || transactionalExecutor != null);
        this.hasDelayedInitialization = hasDelayedInitialization;
        this.transactionalExecutor = transactionalExecutor;
    }

    public final CacheImpl createCacheOrStub() {
        return hasDelayedInitialization ? createCacheStubImpl() : createCacheImpl(null);
    }

    public final void createCacheDelayed(CacheInitializationContext<CacheImpl> context) {
        Preconditions.checkState(hasDelayedInitialization);
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
     * Never called if hasDelayedInitialization == false.
     */
    protected CacheImpl createCacheStubImpl() {
        throw new NotImplementedException();
    }

    /**
     *
     * @param context Null if hasDelayedInitialization == false.
     */
    protected abstract CacheImpl createCacheImpl(CacheInitializationProcessContext context);
}
