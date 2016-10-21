package ru.runa.wfe.commons;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This transaction listener is executed in another thread thus non-blocking EJB call
 *
 * @since 4.2.0
 * @author dofs
 */
public abstract class DeferredTransactionListener implements ITransactionListener, Runnable {
    private static final Log log = LogFactory.getLog(TransactionListeners.class);
    private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    @Override
    public void onTransactionComplete(UserTransaction transaction) {
        log.debug("Scheduling invocation of " + this);
        scheduledExecutorService.schedule(this, 10, TimeUnit.MILLISECONDS);
    }

}
