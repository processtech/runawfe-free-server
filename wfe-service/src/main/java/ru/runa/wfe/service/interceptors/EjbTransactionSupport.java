package ru.runa.wfe.service.interceptors;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.StaleObjectStateException;
import org.springframework.dao.OptimisticLockingFailureException;

import ru.runa.wfe.commons.ITransactionListener;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.service.utils.ApiProperties;
import ru.runa.wfe.user.User;

import com.google.common.base.Throwables;

/**
 * It is important to understand that in a BMT, the message consumed by the MDB is not part of the transaction. When an MDB uses container-managed
 * transactions, the message it handles is a part of the transaction, so if the transaction is rolled back, the consumption of the message is also
 * rolled back, forcing the JMS provider to re-deliver the message. But with bean-managed transactions, the message is not part of the transaction, so
 * if the BMT is rolled back, the JMS provider will not be aware of the transaction’s failure. However, all is not lost, because the JMS provider can
 * still rely on message acknowledgment to determine whether the message was delivered successfully.
 */
public class EjbTransactionSupport {
    private static final Log log = LogFactory.getLog(EjbTransactionSupport.class);
    @Resource
    private EJBContext ejbContext;

    @AroundInvoke
    public Object process(InvocationContext ic) throws Exception {
        try {
            if (ic.getParameters() != null && ic.getParameters().length > 0 && ic.getParameters()[0] instanceof User) {
                User user = (User) ic.getParameters()[0];
                SubjectPrincipalsHelper.validateUser(user);
                UserHolder.set(user);
            }
            UserTransaction transaction = ejbContext.getUserTransaction();
            return invokeWithRetry(ic, transaction, ApiProperties.getRetriesCount());
        } finally {
            UserHolder.reset();
            for (ITransactionListener listener : TransactionListeners.get()) {
                try {
                    listener.onTransactionComplete();
                } catch (Throwable th) {
                    log.error(th);
                }
            }
            TransactionListeners.reset();
        }
    }

    /**
     * Make invocation with retry on optimistic lock failure exception.
     * 
     * @param invocation
     *            current invocation
     * @param transaction
     *            current transaction
     * @return invocation result.
     */
    private Object invokeWithRetry(InvocationContext ic, UserTransaction transaction, int retriesCount) {
        try {
            transaction.begin();
            Object result = ic.proceed();
            transaction.commit();
            return result;
        } catch (Throwable th) {
            Utils.rollbackTransaction(transaction);
            if (th instanceof StaleObjectStateException || th instanceof OptimisticLockingFailureException) {
                log.error(th);
                if (retriesCount > 0) {
                    try {
                        Thread.sleep(ApiProperties.getRetryTimeoutMilliseconds());
                        retriesCount--;
                        return invokeWithRetry(ic, transaction, retriesCount);
                    } catch (InterruptedException e) {
                        log.error(e);
                    }
                }
            }
            throw Throwables.propagate(th);
        }
    }
}
