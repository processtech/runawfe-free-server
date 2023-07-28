package ru.runa.wfe.var;

import com.google.common.base.Preconditions;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.impl.ArchivedByteArrayVariable;
import ru.runa.wfe.var.impl.ArchivedNullVariable;
import ru.runa.wfe.var.impl.CurrentByteArrayVariable;
import ru.runa.wfe.var.impl.CurrentNullVariable;

@CommonsLog
public class VariableCreator {

    private List<VariableType> types;

    @Autowired
    private SerializableToByteArrayConverter serializableToByteArrayConverter;

    @Required
    public void setTypes(List<VariableType> types) {
        this.types = types;
    }

    /**
     * Creates new variable of the corresponding type.
     * 
     * @param value
     *            initial value
     * @return variable
     */
    private Variable create(Object value, boolean isArchive) {
        for (VariableType type : types) {
            if (type.getMatcher().matches(value)) {
                Class<? extends Variable> variableClass = type.getVariableClass(isArchive);
                try {
                    Variable variable = variableClass.newInstance();
                    variable.setConverter(type.getConverter());
                    return variable;
                } catch (Exception e) {
                    throw new InternalApplicationException("Unable to create variable " + variableClass, e);
                }
            }
        }
        throw new InternalApplicationException("No variable found for value " + value);
    }

    /**
     * Creates new variable of the corresponding type. This method does not persisit it.
     * 
     * @param value
     *            initial value
     * @return variable
     */
    public Variable create(Process process, VariableDefinition variableDefinition, Object value) {
        log.debug("Creating variable '" + variableDefinition.getName() + "' in " + process + " with value '" + value + "'"
                + (value != null ? " of " + value.getClass() : ""));
        boolean isArchive = process.isArchived();
        Variable variable;
        if (value == null) {
            variable = isArchive ? new ArchivedNullVariable() : new CurrentNullVariable();
        } else if (variableDefinition.getStoreType() == VariableStoreType.BLOB) {
            log.debug("Using blob storage");
            Preconditions.checkArgument(value instanceof Serializable, "Do not use blob storage on non-serializable value");
            variable = isArchive ? new ArchivedByteArrayVariable() : new CurrentByteArrayVariable();
            variable.setConverter(serializableToByteArrayConverter);
        } else {
            variable = create(value, isArchive);
        }
        variable.setName(variableDefinition.getName());
        variable.setProcess(process);
        variable.setCreateDate(new Date());
        return variable;
    }
}
