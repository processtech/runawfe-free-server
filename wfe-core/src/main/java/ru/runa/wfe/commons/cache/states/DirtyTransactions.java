package ru.runa.wfe.commons.cache.states;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.transaction.Transaction;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Tracking transactions, which change some objects, affecting cache.
 */
@CommonsLog
public class DirtyTransactions<CacheImpl extends CacheImplementation> {

    /**
     * Flag, equals true, if exists at least one dirty transaction; false if no dirty transaction.
     */
    private final AtomicBoolean hasDirty;

    /**
     * Current dirty transactions set.
     */
    private final Map<Transaction, CacheImpl> dirtyTransactions;

    private DirtyTransactions(Transaction transaction, CacheImpl cache) {
        this.hasDirty = new AtomicBoolean(true);
        Map<Transaction, CacheImpl> result = new HashMap<>();
        result.put(transaction, cache);
        this.dirtyTransactions = result;
    }

    private DirtyTransactions(Map<Transaction, CacheImpl> dirtyTransactions) {
        this.hasDirty = new AtomicBoolean(!dirtyTransactions.isEmpty());
        this.dirtyTransactions = dirtyTransactions;
    }

    /**
     * Check if exists changing transaction.
     *
     * @return Return true, if exists at least one changing transaction and false otherwise.
     */
    public boolean isLocked() {
        return hasDirty.get();
    }

    /**
     * Check if transaction is changing some object, affecting cache.
     *
     * @param transaction
     *            Transaction to test dirty state.
     * @return Return true, if transaction is dirty and false otherwise.
     */
    public boolean isDirtyTransaction(Transaction transaction) {
        return dirtyTransactions.containsKey(transaction);
    }

    /**
     * Returns cache instance for transaction or provided cache instance for transaction, not changed cache.
     *
     * @param transaction
     *            Transaction, which requiested cache.
     * @param readCache
     *            Read cache instance.
     * @return Returns cache instance or null.
     */
    public CacheImpl getCache(Transaction transaction, CacheImpl readCache) {
        if (!dirtyTransactions.containsKey(transaction)) {
            return readCache;
        }
        return dirtyTransactions.get(transaction);
    }

    /**
     * Create tracking object with exactly one dirty transaction.
     *
     * @param transaction
     *            Dirty transaction.
     * @param cache
     *            Cache instance for transaction.
     * @return Return tracking object with one dirty transaction.
     */
    public static <CacheImpl extends CacheImplementation> DirtyTransactions<CacheImpl> createOneDirtyTransaction(
            Transaction transaction, CacheImpl cache
    ) {
        return new DirtyTransactions<>(transaction, cache);
    }

    /**
     * Create tracking object with same transactions as in current plus provided transaction. If provided transaction is already dirty, current object
     * returned.
     *
     * @param transaction
     *            Dirty transaction.
     * @param cache
     *            Cache instance for transaction.
     * @return Return tracking object.
     */
    public DirtyTransactions<CacheImpl> addDirtyTransactionAndClone(Transaction transaction, CacheImpl cache) {
        if (dirtyTransactions.containsKey(transaction) && dirtyTransactions.get(transaction) == null && cache == null) {
            return this;
        }
        Map<Transaction, CacheImpl> newDirtySet = new HashMap<>(dirtyTransactions);
        newDirtySet.put(transaction, cache);
        return new DirtyTransactions<>(newDirtySet);
    }

    /**
     * Create tracking object with same transactions as in current minus provided transaction.
     *
     * @param transaction
     *            Completed transaction.
     * @return Return tracking object.
     */
    public DirtyTransactions<CacheImpl> removeDirtyTransactionAndClone(Transaction transaction) {
        Map<Transaction, CacheImpl> newDirtySet = new HashMap<>(dirtyTransactions);
        if (!newDirtySet.containsKey(transaction)) {
            log.error("completed transaction is not in dirty state. It's seems to be an error");
        }
        newDirtySet.remove(transaction);
        return new DirtyTransactions<>(newDirtySet);
    }
}