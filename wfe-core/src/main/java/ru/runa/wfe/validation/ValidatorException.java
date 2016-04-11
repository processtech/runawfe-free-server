package ru.runa.wfe.validation;

/**
 * Thrown from validator with custom message.
 *
 * @author Dofs
 * @since 4.2.0
 */
public class ValidatorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidatorException(String message) {
        super(message);
    }

}
