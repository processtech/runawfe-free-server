package ru.runa.wfe.var;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.CurrentVariableLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;

/**
 * Archived entities are read-lony.
 * <p>
 * But to avoid (at least for now) problems with field-based access in complex entity hierarchies, Archived* entity setters are defined private,
 * or protected if inherited (I defined abstract setters in this class just in case, because Hibernate requires both getters and setters).
 * <p>
 * UPD: VariableLogic.getProcessStateOnTime() creates temporary fake variables which are then proxied;
 * it uses VariableCreator which can access protected setters. Immutability of archive variables is enforced by WfeInterceptor.
 *
 * @see ru.runa.wfe.commons.hibernate.WfeInterceptor
 */
@MappedSuperclass
public abstract class Variable<P extends Process, V> {

    public static int getMaxStringSize() {
        return SystemProperties.getStringVariableValueLength();
    }

    protected Long version;
    protected Converter converter;
    protected String stringValue;
    protected Date createDate;

    @Transient
    public abstract boolean isArchived();

    @Transient
    public abstract Long getId();

    @Transient
    public abstract String getName();
    protected abstract void setName(String name);

    @Transient
    public abstract P getProcess();
    protected abstract void setProcess(P process);

    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    protected abstract void setVersion(Long version);

    @Column(name = "CONVERTER")
    @Type(type = "ru.runa.wfe.commons.hibernate.ConverterEnumType")
    public Converter getConverter() {
        return converter;
    }

    protected abstract void setConverter(Converter converter);

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    protected abstract void setCreateDate(Date createDate);

    @Column(name = "STRINGVALUE", length = 1024)
    public String getStringValue() {
        return stringValue;
    }

    protected abstract void setStringValue(String stringValue);

    /**
     * Get the value of the variable.
     */
    @Transient
    public abstract V getStorableValue();

    /**
     * Set new variable value
     */
    protected abstract void setStorableValue(V object);

    public CurrentVariableLog setValue(ExecutionContext executionContext, Object newValue, VariableDefinition variableDefinition) {
        Object newStorableValue;
        if (supports(newValue)) {
            if (converter != null && converter.supports(newValue)) {
                newStorableValue = converter.convert(executionContext, this, newValue);
            } else {
                converter = null;
                newStorableValue = newValue;
            }
        } else {
            throw new InternalApplicationException(this + " does not support new value '" + newValue + "' of '" + newValue.getClass() + "'");
        }
        Object oldValue = getStorableValue();
        if (newValue == null || converter instanceof SerializableToByteArrayConverter) {
            setStringValue(null);
        } else {
            setStringValue(toString(newValue, variableDefinition));
        }
        if (converter != null && oldValue != null) {
            oldValue = converter.revert(oldValue);
        }
        setStorableValue((V) newStorableValue);
        return getLog(oldValue, newValue, variableDefinition);
    }

    /**
     * Null if called for ArchivedVariable (this can be only when called from VariableLogic.getProcessStateOnTime() which ignores result).
     */
    protected abstract CurrentVariableLog getLog(Object oldValue, Object newValue, VariableDefinition variableDefinition);

    @Transient
    public Object getValue() {
        Object value = getStorableValue();
        if (value != null && converter != null) {
            value = converter.revert(value);
        }
        return value;
    }

    // ATTENTION! Overrides by Current* and Archive* subclasses are the same.
    public boolean supports(Object value) {
        return value != null && converter != null && converter.supports(value);
    }

    // ATTENTION! Overrides by Current* and Archive* subclasses are the same.
    public String toString(Object value, VariableDefinition variableDefinition) {
        String string;
        if (SystemProperties.isV3CompatibilityMode() && value != null && String[].class == value.getClass()) {
            string = Arrays.toString((String[]) value);
        } else if (value instanceof Executor) {
            string = ((Executor) value).getLabel();
        } else {
            string = String.valueOf(value);
        }
        string = Utils.getCuttedString(string, getMaxStringSize());
        return string;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).toString();
    }
}
