package ru.runa.wfe.commons;

import javax.transaction.UserTransaction;

/**
 * Interface for transaction event listeners
 *
 * @since 4.2.0
 * @author dofs
 */
public interface ITransactionListener {

    /**
     * Invoked when EJb transaction committed
     */
    public void onTransactionComplete(UserTransaction transaction);

}
