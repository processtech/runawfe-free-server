package ru.runa.wfe.springframework4.beans.factory.access;

import org.springframework.beans.FatalBeanException;

/**
 * Exception thrown if a bean factory could not be loaded by a bootstrap class.
 *
 * @author Rod Johnson
 * @since 02.12.2002
 */
@SuppressWarnings("serial")
public class BootstrapException extends FatalBeanException {

    /**
     * Create a new BootstrapException with the specified message.
     * 
     * @param msg
     *            the detail message
     */
    public BootstrapException(String msg) {
        super(msg);
    }

    /**
     * Create a new BootstrapException with the specified message and root cause.
     * 
     * @param msg
     *            the detail message
     * @param cause
     *            the root cause
     */
    public BootstrapException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
