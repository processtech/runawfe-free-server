package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.converter.FileVariableToByteArrayConverter;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.converter.StringToByteArrayConverter;

public interface VariableLog extends ProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.VARIABLE;
    }

    @Transient
    default String getVariableName() {
        return getAttributeNotNull(ATTR_VARIABLE_NAME);
    }

    @Transient
    default String getVariableNewValueAttribute() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Transient
    default boolean isFileValue() {
        return "true".equals(getAttribute(ATTR_IS_FILE_VALUE));
    }

    @Transient
    default Object getVariableNewValue() {
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

    @Transient
    default CurrentVariableLog getContentCopy() {
        CurrentVariableLog copyLog;
        if (this instanceof CurrentVariableCreateLog) {
            copyLog = new CurrentVariableCreateLog();
        } else if (this instanceof CurrentVariableUpdateLog) {
            copyLog = new CurrentVariableUpdateLog();
        } else if (this instanceof CurrentVariableDeleteLog) {
            copyLog = new CurrentVariableDeleteLog();
        } else {
            throw new InternalApplicationException("Unexpected " + this);
        }
        copyLog.setBytes(getBytes());
        copyLog.setContent(getContent());
        return copyLog;
    }
}
