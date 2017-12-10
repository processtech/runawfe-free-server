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
 * @deprecated not-friendly for CMT-transactions
 */
@Deprecated
public abstract class DeferredTransactionListener implements ITransactionListener, Runnable {
    private static final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public void onTransactionComplete(UserTransaction transaction) {
        log.debug("Scheduling invocation");
        scheduledExecutorService.schedule(this, 1000, TimeUnit.MILLISECONDS);
    }

}
