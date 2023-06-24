package ru.runa.wfe.commons.cache;

import ru.runa.wfe.commons.TransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.cache.sm.CachingLogic;

/**
 * Resets caches on transaction commit if registered in {@link TransactionListeners}.
 *
 * @author dofs
 */
public class CacheResetTransactionListener implements TransactionListener {
    private final Class<?> forClass;

    public CacheResetTransactionListener(Class<?> forClass) {
        this.forClass = forClass;
    }

    @Override
    public void onTransactionComplete() {
        CachingLogic.resetCaches(forClass);
    }
}
