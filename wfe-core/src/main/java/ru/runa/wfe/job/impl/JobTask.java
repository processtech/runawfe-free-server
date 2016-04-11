package ru.runa.wfe.job.impl;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.TransactionalExecutor;

/**
 * This class supposed to do: 1) general exception handling 2) waiting for
 * system startup completion before job execution
 * 
 * @author Dofs
 * @since 4.1.0
 */
public abstract class JobTask<TE extends TransactionalExecutor> extends TimerTask {
    protected final Log log = LogFactory.getLog(getClass());
    private static boolean systemStartupCompleted = false;
    private TE transactionalExecutor;

    public void setTransactionalExecutor(TE executor) {
        this.transactionalExecutor = executor;
    }

    public TE getTransactionalExecutor() {
        return transactionalExecutor;
    }

    public static void setSystemStartupCompleted(boolean systemStartupCompleted) {
        JobTask.systemStartupCompleted = systemStartupCompleted;
    }

    @Override
    public final void run() {
        if (!systemStartupCompleted) {
            log.debug("Waiting for system startup completion");
            return;
        }
        try {
            execute();
        } catch (Throwable th) {
            log.error("timer task error", th);
        }
    }

    protected abstract void execute() throws Exception;
}
