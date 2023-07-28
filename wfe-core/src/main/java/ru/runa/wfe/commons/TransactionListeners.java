package ru.runa.wfe.commons;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;

/**
 * This class holds current transaction listeners from EJB call and should be
 * used with caution.
 *
 * @author dofs
 * @since 4.2.0
 */
@CommonsLog
public class TransactionListeners {

    private static ThreadLocal<List<TransactionListener>> listeners = new ThreadLocal<List<TransactionListener>>() {
        @Override
        protected List<TransactionListener> initialValue() {
            return Lists.newArrayList();
        }
    };

    public static void addListener(TransactionListener listener, boolean unique) {
        if (unique && listeners.get().contains(listener)) {
            return;
        }
        log.debug("Registered " + listener + " for transaction complete");
        listeners.get().add(listener);
    }

    public static List<TransactionListener> get() {
        return listeners.get();
    }

    public static void reset() {
        listeners.remove();
    }
}
