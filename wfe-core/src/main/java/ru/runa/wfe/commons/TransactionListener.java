package ru.runa.wfe.commons;

/**
 * Interface for transaction event listeners
 *
 * @since 4.2.0
 * @author dofs
 */
public interface TransactionListener {

    /**
     * Invoked when EJb transaction committed
     */
    void onTransactionComplete();

}
