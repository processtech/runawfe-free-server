package ru.runa.wfe.var;

import com.google.common.base.Objects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.Process;

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
public abstract class BaseVariable<P extends Process, V> {

    public static int getMaxStringSize() {
        return SystemProperties.getStringVariableValueLength();
    }

    protected Long version;
    protected Converter converter;
    protected String stringValue;
    protected Date createDate;

    @Transient
    public abstract boolean isArchive();

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

    @Transient
    public Object getValue() {
        Object value = getStorableValue();
        if (value != null && converter != null) {
            value = converter.revert(value);
        }
        return value;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getId()).add("name", getName()).toString();
    }
}
