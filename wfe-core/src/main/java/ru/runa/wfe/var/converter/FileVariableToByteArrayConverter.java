package ru.runa.wfe.var.converter;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.file.IFileVariableStorage;
import ru.runa.wfe.var.matcher.FileVariableMatcher;

/**
 * Besides straightforward functionality this class persist large file variables
 * in local disc storage.
 *
 * @author dofs
 * @since 4.0
 */
@CommonsLog
public class FileVariableToByteArrayConverter extends SerializableToByteArrayConverter {
    private static final long serialVersionUID = 1L;
    private IFileVariableStorage storage;

    @Required
    public void setStorage(IFileVariableStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Object value) {
        if (IFileVariable.class.isAssignableFrom(value.getClass())) {
            return true;
        }
        return FileVariableMatcher.isFileOrListOfFiles(value);
    }

    @Override
    public Object convert(ExecutionContext executionContext, Variable<?> variable, Object object) {
        object = storage.save(executionContext, variable, object);
        log.debug("Saving " + (object != null ? object.getClass() : "null") + " using " + storage);
        return super.convert(executionContext, variable, object);
    }

}
