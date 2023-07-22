package ru.runa.wfe.commons;

import com.google.common.base.Throwables;
import javax.transaction.UserTransaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

// use @Transactional
@Deprecated
public abstract class TransactionalExecutor {
    protected final Log log = LogFactory.getLog(getClass());
    private final UserTransaction transaction;

    public TransactionalExecutor(UserTransaction transaction) {
        this.transaction = transaction;
    }

    public TransactionalExecutor() {
        this(Utils.getUserTransaction());
    }

    public final void executeInTransaction(boolean throwExceptionOnError) {
        try {
            transaction.begin();
            doExecuteInTransaction();
            transaction.commit();
        } catch (Throwable th) {
            Utils.rollbackTransaction(transaction);
            if (throwExceptionOnError) {
                throw Throwables.propagate(th);
            } else {
                log.error("", th);
            }
        }
    }

    protected abstract void doExecuteInTransaction() throws Exception;

}
