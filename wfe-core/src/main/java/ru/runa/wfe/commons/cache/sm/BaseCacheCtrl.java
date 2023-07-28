package ru.runa.wfe.commons.cache.sm;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;
import javax.transaction.Transaction;
import lombok.AllArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

/**
 * Base class for components receiving events on objects change and transaction complete. Components, receiving events must register self in
 * {@link CachingLogic}. All methods must be thread safe and may be called in many threads.
 *
 * @param <CacheImpl> Controlled cache implementation.
 */
public abstract class BaseCacheCtrl<CacheImpl extends CacheImplementation> {
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Cache lifetime control state machine.
     */
    protected final CacheStateMachine<CacheImpl> stateMachine;

    /**
     * Definitions for objects, which may invalidate cache.
     */
    private final List<ListenObjectDefinition> listenObjects;

    protected BaseCacheCtrl(SMCacheFactory<CacheImpl> factory, List<ListenObjectDefinition> listenObjects) {
        this.stateMachine = new CacheStateMachine<>(factory, CachingLogic.class);
        this.listenObjects = listenObjects;
        CachingLogic.registerChangeListener(this);
    }

    public boolean onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        if (log.isTraceEnabled()) {
            String cacheState = BaseCacheCtrl.getCacheStateDescription(stateMachine, transaction);
            String message = cacheState + " On " + changedObject.changeType + " at transaction " + transaction + ": " + changedObject.object + ".";
            log.trace(message);
        }
        stateMachine.onChange(transaction, changedObject);
        return true;
    }

    public final void onBeforeTransactionComplete(Transaction transaction) {
        try {
            if (log.isTraceEnabled()) {
                log.trace(getCacheStateDescription(stateMachine, transaction) + " Preparing transaction " + transaction + " completition.");
            }
            stateMachine.onBeforeTransactionComplete(transaction);
        } catch (Exception e) {
            log.error("onBeforeTransactionComplete(transaction) call failed on " + getClass().getName(), e);
        }
    }

    public final void onAfterTransactionComplete(Transaction transaction) {
        try {
            if (log.isTraceEnabled()) {
                log.trace(getCacheStateDescription(stateMachine, transaction) + " Transaction " + transaction + " is completed.");
            }
            stateMachine.onAfterTransactionComplete(transaction);
        } catch (Exception e) {
            log.error("onTransactionCompleted(transaction) call failed on " + getClass().getName(), e);
        }
    }

    public final void dropCache() {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Dropping cache.");
            }
            stateMachine.dropCache();
        } catch (Exception e) {
            log.error("uninitialize() call failed on " + getClass().getName(), e);
        }
    }

    public final List<Class<?>> getListenObjectTypes() {
        return Lists.transform(listenObjects, new Function<ListenObjectDefinition, Class<?>>() {
            @Override
            public Class<?> apply(ListenObjectDefinition input) {
                return input.listenClass;
            }
        });
    }

    /**
     * Get string description for current cache state (cache is empty or current cache implementation description).
     *
     * @param transaction
     *            Current transaction.
     * @return Return string description for current cache state.
     */
    private static String getCacheStateDescription(CacheStateMachine<?> stateMachine, Transaction transaction) {
        Object cacheImpl = stateMachine.getCacheQuick(transaction);
        return cacheImpl == null ? "(cache is empty)" : "(cache is " + cacheImpl + ")";
    }

    @AllArgsConstructor
    protected static class ListenObjectDefinition {
        /**
         * Class, which change may lead to cache invalidation.
         */
        private final Class<?> listenClass;

    }

}
