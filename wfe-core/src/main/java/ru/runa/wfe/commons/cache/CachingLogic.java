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
package ru.runa.wfe.commons.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Synchronization;
import javax.transaction.Transaction;

import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * Main class for RunaWFE caching. Register {@link ChangeListener} there to receive events on objects change and transaction complete.
 */
public class CachingLogic {

    private static boolean enabled = true;

    /**
     * Map from {@link Transaction} to change listeners, which must be notified on transaction complete. Then transaction change some objects,
     * affected listeners stored there.
     */
    private static Map<Transaction, Set<ChangeListener>> dirtyTransactions = new ConcurrentHashMap<Transaction, Set<ChangeListener>>();

    /**
     * {@link ChangeListener}, which must be notified, when executor related object change.
     */
    static Set<ChangeListener> executorListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when substitution related object change.
     */
    static Set<ChangeListener> substitutionListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when task instance related object change.
     */
    static Set<ChangeListener> taskListeners = new HashSet<ChangeListener>();
    /**
     * {@link ChangeListener}, which must be notified, when process definition related object change.
     */
    static Set<ChangeListener> processDefListeners = new HashSet<ChangeListener>();

    /**
     * Register listener. Listener will be notified on events, according to implemented interfaces.
     * 
     * @param listener
     *            Listener, which must receive events.
     */
    public static synchronized void registerChangeListener(ChangeListener listener) {
        ChangeListenerGuard guarded = new ChangeListenerGuard(listener);
        if (listener instanceof ExecutorChangeListener) {
            executorListeners.add(guarded);
        }
        if (listener instanceof SubstitutionChangeListener) {
            substitutionListeners.add(guarded);
        }
        if (listener instanceof TaskChangeListener) {
            taskListeners.add(guarded);
        }
        if (listener instanceof ProcessDefChangeListener) {
            processDefListeners.add(guarded);
        }
    }

    public static void setEnabled(boolean enabled) {
        ru.runa.wfe.commons.cache.sm.CachingLogic.setEnabled(enabled);
        CachingLogic.enabled = enabled;
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
        if (SystemProperties.useCacheStateMachine()) {
            return;
        } else {
            ru.runa.wfe.commons.cache.sm.CachingLogic.onChange(entity, change, currentState, previousState, propertyNames, types);
        }

        synchronized (CachingLogic.class) {
            if (!enabled) {
                return;
            }
            // TODO move qualification of change listeners to
            // ChangeListener.getInterestedEntityClasses ?
            if (entity instanceof Task || entity instanceof Swimlane || entity instanceof Substitution || entity instanceof SubstitutionCriteria
                    || entity instanceof ExecutorGroupMembership || entity instanceof Actor) {
                onWriteTransaction(taskListeners, entity, change, currentState, previousState, propertyNames, types);
            }
            if (entity instanceof Substitution || entity instanceof SubstitutionCriteria || entity instanceof Actor) {
                onWriteTransaction(substitutionListeners, entity, change, currentState, previousState, propertyNames, types);
            }
            if (entity instanceof Executor || entity instanceof ExecutorGroupMembership) {
                onWriteTransaction(executorListeners, entity, change, currentState, previousState, propertyNames, types);
            }
            if (entity instanceof Deployment) {
                onWriteTransaction(processDefListeners, entity, change, currentState, previousState, propertyNames, types);
            }
        }
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
     *            Listeners to notify.
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
    private static synchronized void onWriteTransaction(Set<ChangeListener> notifyThis, Object changed, Change change, Object[] currentState,
            Object[] previousState, String[] propertyNames, Type[] types) {
        Preconditions.checkNotNull(changed);
        Transaction transaction = Utils.getTransaction();
        if (transaction == null) {
            return;
        }
        Set<ChangeListener> toNotify = dirtyTransactions.get(transaction);
        if (toNotify == null) {
            toNotify = new HashSet<ChangeListener>();
            dirtyTransactions.put(transaction, toNotify);
            DirtyTransactionSynchronization.register(transaction);
        }
        toNotify.addAll(notifyThis);
        for (ChangeListener listener : notifyThis) {
            listener.onChange(transaction, new ChangedObjectParameter(changed, change, currentState, previousState, propertyNames, types));
        }
    }

    /**
     * Called, then thread transaction is completed. If thread transaction change nothing, when do nothing. If thread transaction change some objects,
     * when all related listeners is notified on transaction complete. All related listeners first receive markTransactionComplete event, after what
     * all related listeners receive onTransactionComplete event.
     * 
     * @param transaction
     */
    public static void onTransactionComplete(Transaction transaction) {
        Set<ChangeListener> toNotify = dirtyTransactions.remove(transaction);
        if (toNotify == null) {
            return;
        }
        synchronized (CachingLogic.class) {
            for (ChangeListener listener : toNotify) {
                listener.markTransactionComplete(transaction);
            }
        }
    }

    /**
     * Return cache instance from cache control instance or null. If control instance is locked (already exists transactions, which changing cache)
     * and cache is not initialized returns null; otherwise initialize cache.
     * 
     * @param <CacheImpl>
     *            Type of cache, controlled by cache control component.
     * @param cache
     *            Cache control component.
     * @return Cache instance. May be null.
     */
    public static <CacheImpl extends CacheImplementation> CacheImpl getCacheImplIfNotLocked(CacheControl<CacheImpl> cache) {
        CacheImpl cacheImplTmp = cache.getCache();
        if (cacheImplTmp != null) {
            return cacheImplTmp;
        }
        if (cache.isLocked()) {
            return null;
        }
        synchronized (CachingLogic.class) {
            if (cache.isLocked()) {
                return null;
            }
            return getCacheImpl(cache);
        }
    }

    /**
     * Return cache instance from cache control instance. If control instance already initialized with cache instance, then returning it. If control
     * instance not initialized with cache instance, then cache instance is created and cache control initialized with created cache (if cache
     * instance is not locked).
     * <p>
     * This call can be blocked until change transactions will be complete.
     * </p>
     * 
     * @param <CacheImpl>
     *            Type of cache, controlled by cache control component.
     * @param cache
     *            Cache control component.
     * @return Cache instance. Must be always not null.
     */
    public static <CacheImpl extends CacheImplementation> CacheImpl getCacheImpl(CacheControl<CacheImpl> cache) {
        CacheImpl cacheImplTmp = tryGetCache(cache);
        if (cacheImplTmp != null) {
            return cacheImplTmp;
        }
        boolean isInitiateInProcess = !isWriteTransaction();
        try {
            LogFactory.getLog(cache.getClass()).info("Cache initializing");
            CacheImpl result = cache.buildCache();
            // return temporary cache object on write transaction.
            // Otherwise initiate cache with current cache object.
            if (!isWriteTransaction()) {
                synchronized (CachingLogic.class) {
                    // And notify all threads awaiting cache initialization
                    cache.initCache(result);
                }
            }
            return result;
        } finally {
            if (isInitiateInProcess) {
                // In all case release initialize lock and notify others
                synchronized (CachingLogic.class) {
                    cache.initiateComplete();
                    CachingLogic.class.notifyAll();
                }
            }
        }
    }

    /**
     * Try to get cache implementation from cache control. If no cache implementation in cache control or thread transaction is change some objects,
     * then return null. If cache control is already processing initiation, then current thread blocking until initiation complete.
     * 
     * @param <CacheImpl>
     *            Type of cache, controlled by cache control component.
     * @param cache
     *            Cache control component.
     * @return Cache instance, or null, if cache instance need to be created.
     */
    private static <CacheImpl extends CacheImplementation> CacheImpl tryGetCache(CacheControl<CacheImpl> cache) {
        CacheImpl cacheImplTmp = cache.getCache();
        if (cacheImplTmp != null) {
            return cacheImplTmp;
        }
        synchronized (CachingLogic.class) {
            while (true) {
                try {
                    CacheImpl cacheImpl = cache.getCache();
                    if (cacheImpl != null) {
                        return cacheImpl;
                    }
                    if (!cache.isLocked() && !cache.isInInitiate() && !isWriteTransaction()) {
                        // Cache is steel not initiated and no initiate in
                        // progress - mark it as initiateInProgress and
                        // initiate.
                        cache.initiateInProcess();
                        return null;
                    } else {
                        // Cache is steel not initiated but it locked or
                        // initiating.
                        if (isWriteTransaction()) {
                            // Write transaction must be completed at all case.
                            // Moving to build cache stage
                            return null;
                        } else {
                            // Wait until cache is unlocked or initiate
                            // process finished
                            CachingLogic.class.wait();
                        }
                    }
                } catch (InterruptedException e) {
                    throw Throwables.propagate(e);
                }
            }
        }
    }

    public static void resetAllCaches() {
        Set<ChangeListener> allListeners = Sets.newHashSet();
        allListeners.addAll(executorListeners);
        allListeners.addAll(taskListeners);
        allListeners.addAll(processDefListeners);
        allListeners.addAll(substitutionListeners);
        for (ChangeListener listener : allListeners) {
            listener.uninitialize(CachingLogic.class, Change.REFRESH);
        }
        ru.runa.wfe.commons.cache.sm.CachingLogic.resetAllCaches();
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
}
