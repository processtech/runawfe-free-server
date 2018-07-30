package ru.runa.wfe.commons;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class holds current transaction listeners from EJB call and should be
 * used with caution.
 *
 * @author dofs
 * @since 4.2.0
 */
public class TransactionListeners {
    private static final Log log = LogFactory.getLog(TransactionListeners.class);

    private static ThreadLocal<List<ITransactionListener>> listeners = new ThreadLocal<List<ITransactionListener>>() {
        @Override
        protected List<ITransactionListener> initialValue() {
            return Lists.newArrayList();
        }
    };

    public static void addListener(ITransactionListener listener, boolean unique) {
        if (unique && listeners.get().contains(listener)) {
            return;
        }
        log.debug("Registered " + listener + " for transaction complete");
        listeners.get().add(listener);
    }

    public static List<ITransactionListener> get() {
        return listeners.get();
    }

    public static void reset() {
        listeners.remove();
    }
}
