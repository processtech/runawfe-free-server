package ru.runa.wfe.commons.cache.sm;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

/**
 * Main class for RunaWFE caching. Register {@link BaseCacheCtrl} there to receive events on objects change and transaction complete.
 */
public class CachingLogic {

    /**
     * Flag to enable/disable changes tracking. Stores disableChangesTracking() call count. Used during mass update for performance reason.
     */
    private static AtomicInteger disabledCounter = new AtomicInteger(0);

    /**
     * Map from {@link Transaction} to change listeners, which must be notified on transaction complete. Then transaction change some objects,
     * affected listeners stored there.
     */
    private static ConcurrentMap<Transaction, Set<BaseCacheCtrl<?>>> dirtyTransactions = Maps.newConcurrentMap();

    /**
     * Map from object type to listeners, which must be notifies about object change. This is base structure - only registered classes is present.
     */
    private static ConcurrentMap<Class<?>, Set<BaseCacheCtrl<?>>> objectTypeToListenersRegistered = Maps.newConcurrentMap();

    /**
     * Map from object type to listeners, which must be notifies about object change. All types is present in this map. If no key is present, when
     * listeners must be computed using class hierarchy for notify about subclass changes.
     */
    private static ConcurrentMap<Class<?>, Set<BaseCacheCtrl<?>>> objectTypeToListenersAll = Maps.newConcurrentMap();

    /**
     * Register listener. Listener will be notified on events, according to implemented interfaces.
     *
     * @param listener
     *            Listener, which must receive events.
     */
    public static synchronized void registerChangeListener(BaseCacheCtrl<?> listener) {
        for (Class<?> clazz : listener.getListenObjectTypes()) {
            Set<BaseCacheCtrl<?>> listeners = objectTypeToListenersRegistered.get(clazz);
            if (listeners == null) {
                listeners = Sets.newConcurrentHashSet();
                objectTypeToListenersRegistered.put(clazz, listeners);
            }
            listeners.add(listener);
        }
        objectTypeToListenersAll.clear();
    }

    /**
     * Get change listeners for specified class.
     *
     * @param clazz
     *            Changed class.
     * @return Return change listeners for specified class.
     */
    private static Set<BaseCacheCtrl<?>> getChangeListeners(Class<?> clazz) {
        Set<BaseCacheCtrl<?>> result = objectTypeToListenersAll.get(clazz);
        if (result != null) {
            return result;
        }
        synchronized (CachingLogic.class) {
            result = Sets.newLinkedHashSet();
            Class<?> superclass = clazz;
            do {
                Set<BaseCacheCtrl<?>> registered = objectTypeToListenersRegistered.get(superclass);
                if (registered != null) {
                    result.addAll(registered);
                }
                superclass = superclass.getSuperclass();
            } while (superclass != null);
            objectTypeToListenersAll.put(clazz, result);
        }
        return result;
    }

    /**
     * Disables changes tracking. Used with mass update for performance reason.
     *
     * @see #enableChangesTracking()
     */
    public static void disableChangesTracking() {
        disabledCounter.incrementAndGet();
    }

    /**
     * Re-enables changes tracking.
     */
    public static void enableChangesTracking() {
        int i = disabledCounter.decrementAndGet();
        Preconditions.checkState(i >= 0);
    }

    /**
     * Check current thread transaction type.
     *
     * @return If transaction change some objects, return true; return false otherwise.
     */
    private static boolean isWriteTransaction() {
        Transaction transaction = Utils.getTransaction();
        if (transaction == null) {
            return false;
        }
        return dirtyTransactions.containsKey(transaction);
    }

    /**
     * Notify registered listeners on entity change.
     *
     * @param entity
     *            Changed object.
     * @param change
     *            operation type
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     */
    public static void onChange(Object entity, Change change, Object[] currentState, Object[] previousState, String[] propertyNames) {
        if (disabledCounter.get() > 0) {
            return;
        }
        Preconditions.checkNotNull(entity);
        Transaction transaction = Utils.getTransaction();
        if (transaction == null) {
            return;
        }
        Set<BaseCacheCtrl<?>> toNotify = dirtyTransactions.get(transaction);
        if (toNotify == null) {
            toNotify = Sets.newConcurrentHashSet();
            dirtyTransactions.put(transaction, toNotify);
            DirtyTransactionSynchronization.register(transaction);
        }
        Set<BaseCacheCtrl<?>> listeners = getChangeListeners(entity.getClass());
        if (listeners != null) {
            toNotify.addAll(listeners);
            for (BaseCacheCtrl<?> listener : listeners) {
                listener.onChange(transaction, new ChangedObjectParameter(entity, change, currentState, previousState, propertyNames));
            }
        }
    }

    /**
     * Called before transaction complete.
     *
     * @param transaction
     *            Transaction, which would be completed.
     */
    private static void onBeforeTransactionComplete(Transaction transaction) {
        Set<BaseCacheCtrl<?>> toNotify = dirtyTransactions.get(transaction);
        if (toNotify == null) {
            return;
        }
        for (BaseCacheCtrl<?> listener : toNotify) {
            listener.onBeforeTransactionComplete(transaction);
        }
    }

    /**
     * Called, then thread transaction is completed. If thread transaction change nothing, when do nothing. If thread transaction change some objects,
     * when all related listeners is notified on transaction complete.
     *
     * @param transaction
     *            Transaction, which completed.
     */
    private static void onAfterTransactionComplete(Transaction transaction) {
        Set<BaseCacheCtrl<?>> toNotify = dirtyTransactions.remove(transaction);
        if (toNotify == null) {
            return;
        }
        for (BaseCacheCtrl<?> listener : toNotify) {
            listener.onAfterTransactionComplete(transaction);
        }
    }

    /**
     * Get or create cache. Cache (or stub) will be returned in all case.
     *
     * @param stateMachine
     *            Cache lifetime state machine.
     * @return Return cache implementation (always not null).
     */
    public static <CacheImpl extends CacheImplementation> CacheImpl getCacheImpl(CacheStateMachine<CacheImpl> stateMachine) {
        return stateMachine.getCache(getTransactionToGetCache(), isWriteTransaction());
    }

    /**
     * Get or create cache. If changing transaction is exists, then returns null.
     *
     * @param stateMachine
     *            Cache lifetime state machine.
     * @return Return cache implementation or null, if cache is locked (dirty transaction exists).
     */
    public static <CacheImpl extends CacheImplementation> CacheImpl getCacheImplIfNotLocked(CacheStateMachine<CacheImpl> stateMachine) {
        return stateMachine.getCacheIfNotLocked(getTransactionToGetCache(), isWriteTransaction());
    }

    private static Transaction getTransactionToGetCache() {
        val transaction = Utils.getTransaction();
        return transaction != null ? transaction : WrongAccessTransaction.getInstance();
    }

    public static void dropAllCaches() {
        for (Set<BaseCacheCtrl<?>> listeners : objectTypeToListenersRegistered.values()) {
            for (BaseCacheCtrl<?> listener : listeners) {
                listener.dropCache();
            }
        }
    }

    /**
     * Callback object to receive dirty transaction commit/rollback events.
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static class DirtyTransactionSynchronization implements Synchronization {

        private final Transaction transaction;

        @Override
        public void beforeCompletion() {
            CachingLogic.onBeforeTransactionComplete(transaction);
        }

        @Override
        public void afterCompletion(int status) {
            CachingLogic.onAfterTransactionComplete(transaction);
        }

        public static void register(Transaction transaction) {
            try {
                transaction.registerSynchronization(new DirtyTransactionSynchronization(transaction));
            } catch (Exception e) {
                throw new InternalApplicationException("Unexpected error on cache synchronization registration", e);
            }
        }
    }

    @CommonsLog
    private static class WrongAccessTransaction implements Transaction {

        private final static Transaction instance = new WrongAccessTransaction();

        private WrongAccessTransaction() {
        }

        public static Transaction getInstance() {
            StringBuilder message = new StringBuilder("Non transactional access detected:").append("\n");
            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                message.append("\t").append(element).append("\n");
            }
            log.error(message);
            return instance;
        }

        @Override
        public void commit() {
        }

        @Override
        public boolean delistResource(XAResource xaRes, int flag) {
            return false;
        }

        @Override
        public boolean enlistResource(XAResource xaRes) {
            return false;
        }

        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public void registerSynchronization(Synchronization sync) {
        }

        @Override
        public void rollback() {
        }

        @Override
        public void setRollbackOnly() {
        }
    }
}
