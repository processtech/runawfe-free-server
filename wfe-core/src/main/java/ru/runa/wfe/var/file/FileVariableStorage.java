package ru.runa.wfe.var.file;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Variable;

/**
 * Allows alternative file storage.
 *
 * @author dofs
 * @since 4.2.0
 */
public interface FileVariableStorage {

    /**
     * Stores file variable data object (can be IFilevariable or
     * List<FileVariable>).
     *
     * @return converted instance to save in internal database
     */
    public Object save(ExecutionContext executionContext, Variable<?> variable, Object object);

}
