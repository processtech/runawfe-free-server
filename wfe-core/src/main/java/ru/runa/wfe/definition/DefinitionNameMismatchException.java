package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that definition name differs from the name of existing definition (during redeploy).
 */
public class DefinitionNameMismatchException extends InternalApplicationException {
    private static final long serialVersionUID = -2137340395617831247L;
    private final String expectedProcessDefinitionName;
    private final String givenProcessDefinitionName;

    public DefinitionNameMismatchException(String expectedProcessDefinitionName, String givenProcessDefinitionName) {
        super("Expected definition name " + expectedProcessDefinitionName);
        this.expectedProcessDefinitionName = expectedProcessDefinitionName;
        this.givenProcessDefinitionName = givenProcessDefinitionName;
    }

    public String getExpectedProcessDefinitionName() {
        return expectedProcessDefinitionName;
    }

    public String getGivenProcessDefinitionName() {
        return givenProcessDefinitionName;
    }
}
