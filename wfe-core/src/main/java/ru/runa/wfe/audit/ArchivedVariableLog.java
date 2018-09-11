package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.var.converter.FileVariableToByteArrayConverter;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.converter.StringToByteArrayConverter;

@Entity
@DiscriminatorValue(value = ",")
public abstract class ArchivedVariableLog extends ArchivedProcessLog implements VariableLog {

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE;
    }

    @Override
    @Transient
    public String getVariableName() {
        return getAttributeNotNull(ATTR_VARIABLE_NAME);
    }

    @Override
    @Transient
    public String getVariableNewValueAttribute() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    public boolean isFileValue() {
        return "true".equals(getAttribute(ATTR_IS_FILE_VALUE));
    }

    @Override
    @Transient
    public Object getVariableNewValue() {
        byte[] bytes = getBytes();
        if (bytes != null) {
            if (isFileValue()) {
                return new FileVariableToByteArrayConverter().revert(bytes);
            }
            try {
                return new SerializableToByteArrayConverter().revert(bytes);
            } catch (Exception e) {
                return new StringToByteArrayConverter().revert(bytes);
            }
        }
        return getVariableNewValueAttribute();
    }
}
