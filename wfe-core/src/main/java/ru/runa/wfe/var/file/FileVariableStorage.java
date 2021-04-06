package ru.runa.wfe.var.file;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Variable;

/**
 * Allows alternative file storage.
 *
 * @author dofs
 */
public interface FileVariableStorage {

    /**
     * Stores file variable(s).
     *
     * @param object FileVariable, or List of FileVariable, or empty list.
     * @return Some serializable handler instance (a reference to file saved into storage) to save in database.
     */
    Object save(ExecutionContext executionContext, Variable variable, Object object);
}
