package ru.runa.wfe.commons.cache;

import javax.transaction.UserTransaction;
import ru.runa.wfe.commons.TransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.cache.sm.CachingLogic;

/**
 * Resets caches on transaction commit if registered in {@link TransactionListeners}.
 *
 * @author dofs
 */
public class CacheResetTransactionListener implements TransactionListener {

    @Override
    public void onTransactionComplete(UserTransaction transaction) {
        CachingLogic.dropAllCaches();
    }
}
