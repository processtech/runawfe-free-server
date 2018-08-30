package ru.runa.wfe.var;

import com.google.common.base.Objects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import org.hibernate.annotations.Type;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.Process;

@MappedSuperclass
public abstract class BaseVariable<P extends Process, T> {

    public static int getMaxStringSize() {
        return SystemProperties.getStringVariableValueLength();
    }

    private Long version;
    protected Converter converter;
    private String stringValue;
    private Date createDate;

    @Transient
    public abstract boolean isArchive();

    @Transient
    public abstract Long getId();
    protected abstract void setId(Long id);

    @Transient
    public abstract String getName();
    public abstract void setName(String name);

    @Transient
    public abstract P getProcess();
    public abstract void setProcess(P process);

    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "CONVERTER")
    @Type(type = "ru.runa.wfe.commons.hibernate.ConverterEnumType")
    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "STRINGVALUE", length = 1024)
    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Get the value of the variable.
     */
    @Transient
    public abstract T getStorableValue();

    /**
     * Set new variable value
     */
    protected abstract void setStorableValue(T object);

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
