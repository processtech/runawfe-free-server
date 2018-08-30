package ru.runa.wfe.var.converter;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableStorage;
import ru.runa.wfe.var.matcher.FileVariableMatcher;

/**
 * Besides straightforward functionality this class persist large file variables
 * in local disc storage.
 *
 * @author dofs
 * @since 4.0
 *
 * TODO Stored as java-serialized, which makes impossible to rename/refactor its structure in any way. Instead should store JSON or something.
 */
@CommonsLog
public class FileVariableToByteArrayConverter extends SerializableToByteArrayConverter {
    private static final long serialVersionUID = 1L;
    private FileVariableStorage storage;

    @Required
    public void setStorage(FileVariableStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Object value) {
        // TODO Duplicates non-static method FileVariableMatcher.matches().
        return FileVariable.class.isAssignableFrom(value.getClass()) || FileVariableMatcher.isFileOrListOfFiles(value);
    }

    @Override
    public Object convert(ExecutionContext executionContext, CurrentVariable<?> variable, Object object) {
        object = storage.save(executionContext, variable, object);
        log.debug("Saving " + (object != null ? object.getClass() : "null") + " using " + storage);
        return super.convert(executionContext, variable, object);
    }
}
