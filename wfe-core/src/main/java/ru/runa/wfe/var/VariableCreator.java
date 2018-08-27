package ru.runa.wfe.var;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.impl.CurrentByteArrayVariable;
import ru.runa.wfe.var.impl.CurrentNullVariable;

import com.google.common.base.Preconditions;

public class VariableCreator {
    private static final Log log = LogFactory.getLog(VariableCreator.class);

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
    private CurrentVariable<?> create(Object value) {
        for (VariableType type : types) {
            if (type.getMatcher().matches(value)) {
                try {
                    CurrentVariable<?> variable = type.getVariableClass().newInstance();
                    variable.setConverter(type.getConverter());
                    return variable;
                } catch (Exception e) {
                    throw new InternalApplicationException("Unable to create variable " + type.getVariableClass(), e);
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
    public CurrentVariable<?> create(CurrentProcess process, VariableDefinition variableDefinition, Object value) {
        log.debug("Creating variable '" + variableDefinition.getName() + "' in " + process + " with value '" + value + "'"
                + (value != null ? " of " + value.getClass() : ""));
        CurrentVariable<?> variable;
        if (value == null) {
            variable = new CurrentNullVariable();
        } else if (variableDefinition.getStoreType() == VariableStoreType.BLOB) {
            log.debug("Using blob storage");
            Preconditions.checkArgument(value instanceof Serializable, "Do not use blob storage on non-serializable value");
            variable = new CurrentByteArrayVariable();
            variable.setConverter(serializableToByteArrayConverter);
        } else {
            variable = create(value);
        }
        variable.setName(variableDefinition.getName());
        variable.setProcess(process);
        variable.setCreateDate(new Date());
        return variable;
    }

}
