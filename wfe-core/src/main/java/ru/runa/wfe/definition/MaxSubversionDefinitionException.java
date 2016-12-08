package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Thrown when trying to parse invalid process definition.
 * 
 * @author Dofs
 */
public class MaxSubversionDefinitionException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    private final String definitionName;

    public MaxSubversionDefinitionException(String definitionName, Throwable cause) {
        super(definitionName, cause);
        this.definitionName = definitionName;
    }

    public MaxSubversionDefinitionException(String definitionName, String message) {
        super(message);
        this.definitionName = definitionName;
    }

    public MaxSubversionDefinitionException(String definitionName, String message, Throwable cause) {
        super(message, cause);
        this.definitionName = definitionName;
    }

    public String getDefinitionName() {
        return definitionName;
    }

}
