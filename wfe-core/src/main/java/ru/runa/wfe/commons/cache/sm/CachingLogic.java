/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.cache.sm;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;

/**
 * Main class for RunaWFE caching. Register {@link ChangeListener} there to receive events on objects change and transaction complete.
 */
public class CachingLogic {

    /**
     * Flag to enable/disable changes tracking. It may be set to false in case of mass update for performance reason. Stores disable call count. To
     * enable changes tracking enables must be called the same times.
     */
    private static AtomicInteger enabled = new AtomicInteger(0);

    /**
     * Map from {@link Transaction} to change listeners, which must be notified on transaction complete. Then transaction change some objects,
     * affected listeners stored there.
     */
    private static ConcurrentMap<Transaction, Set<ChangeListener>> dirtyTransactions = Maps.newConcurrentMap();

    /**
     * Map from object type to listeners, which must be notifies about object change. This is base structure - only registered classes is present.
     */
    private static ConcurrentMap<Class<?>, Set<ChangeListener>> objectTypeToListenersRegistered = Maps.newConcurrentMap();

    /**
     * Map from object type to listeners, which must be notifies about object change. All types is present in this map. If no key is present, when
     * listeners must be computed using class hierarchy for notify about subclass changes.
     */
    private static ConcurrentMap<Class<?>, Set<ChangeListener>> objectTypeToListenersAll = Maps.newConcurrentMap();

    /**
     * Register listener. Listener will be notified on events, according to implemented interfaces.
     *
     * @param listener
     *            Listener, which must receive events.
     */
    public static synchronized void registerChangeListener(ChangeListener listener) {
        ChangeListenerGuard guarded = new ChangeListenerGuard(listener);
        for (Class<?> clazz : listener.getListenObjectTypes()) {
            Set<ChangeListener> listeners = objectTypeToListenersRegistered.get(clazz);
            if (listeners == null) {
                listeners = Sets.newConcurrentHashSet();
                objectTypeToListenersRegistered.put(clazz, listeners);
            }
            listeners.add(guarded);
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
    private static Set<ChangeListener> getChangeListeners(Class<?> clazz) {
        Set<ChangeListener> result = objectTypeToListenersAll.get(clazz);
        if (result != null) {
            return result;
        }
        synchronized (CachingLogic.class) {
            result = Sets.newLinkedHashSet();
            Class<?> superclass = clazz;
            do {
                Set<ChangeListener> registered = objectTypeToListenersRegistered.get(superclass);
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
     * Enables/disables changes tracking. It may be set to false in case of mass update for performance reason.
     *
     * @param enabled
     *            Flag, equals true, to enable changes tracking and false otherwise.
     */
    public static void setEnabled(boolean enabled) {
        if (enabled) {
            CachingLogic.enabled.decrementAndGet();
        } else {
            CachingLogic.enabled.incrementAndGet();
        }
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
     * @param types
     *            Property types.
     */
    public static void onChange(Object entity, Change change, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (enabled.get() > 0) {
            return;
        }
        onWriteTransaction(getChangeListeners(entity.getClass()), entity, change, currentState, previousState, propertyNames, types);
    }

    /**
     * Check current thread transaction type.
     *
     * @return If transaction change some objects, return true; return false otherwise.
     */
    public static boolean isWriteTransaction() {
        Transaction transaction = Utils.getTransaction();
        if (transaction == null) {
            return false;
        }
        return dirtyTransactions.containsKey(transaction);
    }

    /**
     * Notifies given listeners.
     *
     * @param notifyThis
     *            Listeners to notify. May be null.
     * @param object
     *            Changed object.
     * @param currentState
     *            Current state of object properties.
     * @param previousState
     *            Previous state of object properties.
     * @param propertyNames
     *            Property names (same order as in currentState).
     * @param types
     *            Property types.
     */
    private static void onWriteTransaction(Set<ChangeListener> notifyThis, Object changed, Change change, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        Preconditions.checkNotNull(changed);
        Transaction transaction = Utils.getTransaction();
        if (transaction == null) {
            return;
        }
        Set<ChangeListener> toNotify = dirtyTransactions.get(transaction);
        if (toNotify == null) {
            toNotify = Sets.<ChangeListener>newConcurrentHashSet();
            dirtyTransactions.put(transaction, toNotify);
            DirtyTransactionSynchronization.register(transaction);
        }
        if (notifyThis != null) {
            toNotify.addAll(notifyThis);
            for (ChangeListener listener : notifyThis) {
                listener.onChange(transaction, new ChangedObjectParameter(changed, change, currentState, previousState, propertyNames, types));
            }
        }
    }

    /**
     * Called before transaction complete.
     *
     * @param transaction
     *            Transaction, which would be completed.
     */
    public static void beforeTransactionComplete(Transaction transaction) {
        Set<ChangeListener> toNotify = dirtyTransactions.get(transaction);
        if (toNotify == null) {
            return;
        }
        for (ChangeListener listener : toNotify) {
            listener.beforeTransactionComplete(transaction);
        }
    }

    /**
     * Called, then thread transaction is completed. If thread transaction change nothing, when do nothing. If thread transaction change some objects,
     * when all related listeners is notified on transaction complete.
     *
     * @param transaction
     *            Transaction, which completed.
     */
    public static void onTransactionComplete(Transaction transaction) {
        Set<ChangeListener> toNotify = dirtyTransactions.remove(transaction);
        if (toNotify == null) {
            return;
        }
        for (ChangeListener listener : toNotify) {
            listener.onTransactionCompleted(transaction);
        }
    }

    /**
     * Get or create cache. Cache (or proxy) will be returned in all case.
     *
     * @param stateMachine
     *            Cache lifetime state machine.
     * @return Return cache implementation (always not null).
     */
    public static <CacheImpl extends CacheImplementation, StateContext> CacheImpl getCacheImpl(
            CacheStateMachine<CacheImpl, StateContext> stateMachine) {
        return stateMachine.getCache(getTransactionToGetCache(), isWriteTransaction());
    }

    /**
     * Get or create cache. If changing transaction is exists, when returns null.
     *
     * @param stateMachine
     *            Cache lifetime state machine.
     * @return Return cache implementation or null, if cache is locked (dirty transaction exists).
     */
    public static <CacheImpl extends CacheImplementation, StateContext> CacheImpl getCacheImplIfNotLocked(
            CacheStateMachine<CacheImpl, StateContext> stateMachine) {
        return stateMachine.getCacheIfNotLocked(getTransactionToGetCache(), isWriteTransaction());
    }

    private static Transaction getTransactionToGetCache() {
        Transaction transaction = Utils.getTransaction();
        return transaction != null ? transaction : WrongAccessTransaction.getInstance();
    }

    public static void resetAllCaches() {
        Set<ChangeListener> allListeners = Sets.newHashSet();
        for (Set<ChangeListener> listeners : objectTypeToListenersRegistered.values()) {
            allListeners.addAll(listeners);
        }
        for (ChangeListener listener : allListeners) {
            listener.uninitialize(CachingLogic.class, Change.REFRESH);
        }
    }

    /**
     * Callback object to receive dirty transaction commit/rollback events.
     */
    static class DirtyTransactionSynchronization implements Synchronization {

        private final Transaction transaction;

        public DirtyTransactionSynchronization(Transaction transaction) {
            super();
            this.transaction = transaction;
        }

        @Override
        public void beforeCompletion() {
            CachingLogic.beforeTransactionComplete(transaction);
        }

        @Override
        public void afterCompletion(int status) {
            CachingLogic.onTransactionComplete(transaction);
        }

        public static void register(Transaction transaction) {
            try {
                transaction.registerSynchronization(new DirtyTransactionSynchronization(transaction));
            } catch (Exception e) {
                throw new InternalApplicationException("Unexpected error on cache synchronization registration", e);
            }
        }
    }

    static class WrongAccessTransaction implements Transaction {

        private static final Log log = LogFactory.getLog(WrongAccessTransaction.class);

        private final static Transaction instance = new WrongAccessTransaction();

        private WrongAccessTransaction() {
        }

        public static Transaction getInstance() {
            StringBuilder message = new StringBuilder("Non transactional access detected:").append("\n");
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (stack != null) {
                for (StackTraceElement element : stack) {
                    message.append("\t").append(element).append("\n");
                }
            }
            log.error(message);
            return instance;
        }

        @Override
        public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException,
                SystemException {
        }

        @Override
        public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
            return false;
        }

        @Override
        public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
            return false;
        }

        @Override
        public int getStatus() throws SystemException {
            return 0;
        }

        @Override
        public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
        }

        @Override
        public void rollback() throws IllegalStateException, SystemException {
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException, SystemException {
        }
    }
}
