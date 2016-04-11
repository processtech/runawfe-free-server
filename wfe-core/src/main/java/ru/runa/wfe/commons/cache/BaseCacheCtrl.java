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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.PropertyResources;

/**
 * Base implementation of cache control objects.
 * 
 * @author Konstantinov Aleksey
 * @param <CacheImpl>
 *            Controlled cache implementation.
 */
public abstract class BaseCacheCtrl<CacheImpl extends CacheImplementation> implements CacheControl<CacheImpl>, ChangeListener {
    private static final PropertyResources RESOURCES = new PropertyResources("cache.properties", true, false);

    /**
     * Smart cache parameter name.
     */
    private static final String SMART_CACHE = "smart_cache";

    /**
     * Logging support.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Current cache implementation. May be null, if no cache implementation initialized.
     */
    private final AtomicReference<CacheImpl> impl = new AtomicReference<CacheImpl>(null);

    /**
     * Flag, equals true, if cache currently initializing to set current cache implementation (impl).
     */
    private final AtomicBoolean isInitiateInProcess = new AtomicBoolean(false);

    /**
     * Stores dirty threads for cache control.
     */
    private final DirtyTransactionsStorage dirtyThreads = new DirtyTransactionsStorage();

    @Override
    public final CacheImpl getCache() {
        return impl.get();
    }

    @Override
    public final void initCache(CacheImpl cache) {
        cache.commitCache();
        impl.set(cache);
        log.info("Cache is initialized");
    }

    @Override
    public final void initiateComplete() {
        isInitiateInProcess.set(false);
    }

    @Override
    public final void initiateInProcess() {
        isInitiateInProcess.set(true);
    }

    @Override
    public final boolean isInInitiate() {
        return isInitiateInProcess.get();
    }

    @Override
    public final boolean isLocked() {
        return dirtyThreads.isDirtyExist();
    }

    @Override
    public final void onChange(Transaction transaction, ChangedObjectParameter changedObject) {
        registerChange(transaction);
        log.debug("On " + changedObject.changeType + ": " + changedObject.object);
        if (!isSmartCache()) {
            uninitialize(changedObject);
        } else {
            doOnChange(changedObject);
        }
    }

    @Override
    public final void markTransactionComplete(Transaction transaction) {
        if (dirtyThreads.resetDirty(transaction)) {
            CachingLogic.class.notifyAll();
        }
        doMarkTransactionComplete();
    }

    /**
     * <b>Override this method if you need some additional actions on {@link #onChange(ChangedObjectParameter)}.</b>
     * <p/>
     * 
     * Called, then changed one of predefined object (e. q. specific sub interface exists). If smart cache capability is off, then this method is not
     * called (called {@link #uninitialize(ChangedObjectParameter)}).
     * 
     * @param changedObject
     *            Changed object data.
     */
    protected abstract void doOnChange(ChangedObjectParameter changedObject);

    /**
     * <b>Override this method if you need some additional actions on {@link #markTransactionComplete()}. Default implementation is suitable for most
     * case.</b>
     * <p/>
     * 
     * Called, then transaction in current thread is completed. Cache controller must mark transaction as completed, but must not recreate cache.
     * <p/>
     * Cache recreation may be done in {@link #onTransactionComplete()}, then all caches is marked transaction.
     * <p/>
     * {@link CachingLogic} guarantees, what all caches receive {@link #markTransactionComplete()}, and only after what all caches receive
     * {@link #onTransactionComplete()}.
     */
    protected void doMarkTransactionComplete() {
    }

    /**
     * Drops current cache implementation.
     * 
     * @param object
     *            Changed object, which leads to cache drop.
     */
    @Override
    public void uninitialize(Object object, Change change) {
        if (impl.get() != null) {
            log.info("Cache is uninitialized. Due to " + change + " of " + object);
        }
        impl.set(null);
    }

    /**
     * Drops current cache implementation.
     * 
     * @param object
     *            Changed object, which leads to cache drop.
     */
    protected void uninitialize(ChangedObjectParameter changedObject) {
        uninitialize(changedObject.object, changedObject.changeType);
    }

    /**
     * Register current Thread as changing. After thread transaction completes it will be removed from changing threads.
     * 
     * @param transaction
     *            Transaction, which change some object, affecting cache state.
     */
    private void registerChange(Transaction transaction) {
        dirtyThreads.markAsDirty(transaction);
    }

    /**
     * Check if current cache is 'smart'. If cache is not 'smart' it will completely drop cache implementation on all affecting changes; 'smart' cache
     * tries to remove only affected elements from cache.
     * 
     * @return true, if cache is 'smart'; false otherwise.
     */
    protected boolean isSmartCache() {
        if (RESOURCES.getBooleanProperty(SMART_CACHE, false)) {
            return RESOURCES.getBooleanProperty(this.getClass().getName() + "." + SMART_CACHE, true);
        }
        return false;
    }

    /**
     * Stores set of dirty transactions for cache control.
     */
    class DirtyTransactionsStorage {
        /**
         * Flag, equals true if some dirty transaction exists and false otherwise.
         */
        private final AtomicBoolean hasDirty = new AtomicBoolean(false);

        /**
         * Set of transactions, which makes changes, affecting cache. After transaction completes, it removes from this set.
         */
        private final Set<Transaction> dirtyTransactions = new HashSet<Transaction>();

        /**
         * Check if dirty transactions exists for cache control.
         * 
         * @return true if dirty transactions exists and false otherwise.
         */
        public boolean isDirtyExist() {
            return hasDirty.get();
        }

        /**
         * Mark transaction as dirty thread.
         * 
         * @param transaction
         *            Transaction, which change some object, affecting cache state.
         */
        public synchronized void markAsDirty(Transaction transaction) {
            dirtyTransactions.add(transaction);
            hasDirty.set(true);
        }

        /**
         * Reset dirty flag from transaction.
         * 
         * @param transaction
         *            Commit or rollback transaction.
         * @return true, if no dirty transactions in cache control and false otherwise.
         */
        public synchronized boolean resetDirty(Transaction transaction) {
            dirtyTransactions.remove(transaction);
            if (!dirtyTransactions.isEmpty()) {
                return false;
            }
            hasDirty.set(false);
            return true;
        }
    }
}
