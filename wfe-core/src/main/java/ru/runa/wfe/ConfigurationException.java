package ru.runa.wfe;

/**
 * Signals about misconfiguration or misuse.
 * 
 * @author dofs
 * @since 4.2.0
 */
public class ConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
