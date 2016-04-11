package ru.runa.wfe.service.exceptions;

public class DefinitionHasProcessesException extends RuntimeException {

    private static final String MESSAGE = "The process definition has associated processes";
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public DefinitionHasProcessesException() {
        super(MESSAGE);
    }

    public DefinitionHasProcessesException(String message) {
        super(message);
    }

}
