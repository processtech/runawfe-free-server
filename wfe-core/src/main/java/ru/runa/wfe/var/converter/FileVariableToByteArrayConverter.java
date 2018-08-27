package ru.runa.wfe.var.converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 */
public class FileVariableToByteArrayConverter extends SerializableToByteArrayConverter {
    private static final long serialVersionUID = 1L;
    private static Log log = LogFactory.getLog(FileVariableToByteArrayConverter.class);
    private FileVariableStorage storage;

    @Required
    public void setStorage(FileVariableStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supports(Object value) {
        if (FileVariable.class.isAssignableFrom(value.getClass())) {
            return true;
        }
        return FileVariableMatcher.isFileOrListOfFiles(value);
    }

    @Override
    public Object convert(ExecutionContext executionContext, CurrentVariable<?> variable, Object object) {
        object = storage.save(executionContext, variable, object);
        log.debug("Saving " + (object != null ? object.getClass() : "null") + " using " + storage);
        return super.convert(executionContext, variable, object);
    }

}
