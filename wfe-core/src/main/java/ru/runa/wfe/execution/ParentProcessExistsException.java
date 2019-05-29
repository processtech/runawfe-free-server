package ru.runa.wfe.execution;

import ru.runa.wfe.InternalApplicationException;

/**
 * Unexpected existance of parent process.
 * 
 * @author Dofs
 * @since 4.0
 */
public class ParentProcessExistsException extends InternalApplicationException {
    private static final long serialVersionUID = 1833565682886180147L;

    private final String definitionName;
    private final String parentDefinitionName;

    public ParentProcessExistsException(String definitionName, String parentDefinitionName) {
        this.definitionName = definitionName;
        this.parentDefinitionName = parentDefinitionName;
    }

    public String getDefinitionName() {
        return definitionName;
    }

    public String getParentDefinitionName() {
        return parentDefinitionName;
    }
}
