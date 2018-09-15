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
 * @author dofs
 */
public abstract class DeferredTransactionListener implements TransactionListener, Runnable {
    private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public void onTransactionComplete(UserTransaction transaction) {
        log.debug("Scheduling invocation");
        scheduledExecutorService.schedule(this, 10, TimeUnit.MILLISECONDS);
    }
}
