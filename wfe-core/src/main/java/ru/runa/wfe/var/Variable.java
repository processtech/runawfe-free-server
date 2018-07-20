/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.var;

import com.google.common.base.Objects;
import java.util.Arrays;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.audit.VariableCreateLog;
import ru.runa.wfe.audit.VariableDeleteLog;
import ru.runa.wfe.audit.VariableLog;
import ru.runa.wfe.audit.VariableUpdateLog;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;

/**
 * Base class for classes that store variable values in the database.
 */
@Entity
@Table(name = "BPM_VARIABLE", uniqueConstraints = { @UniqueConstraint(name = "UK_VARIABLE_PROCESS", columnNames = { "PROCESS_ID", "NAME" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Variable<T> {

    public static int getMaxStringSize() {
        return SystemProperties.getStringVariableValueLength();
    }

    protected Long id;
    private Long version;
    private String name;
    private Process process;
    private Converter converter;
    private String stringValue;
    private Date createDate;

    public Variable() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_VARIABLE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "NAME", length = 1024)
    @Index(name = "IX_VARIABLE_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "CONVERTER")
    @Type(type = "ru.runa.wfe.commons.hibernate.ConverterEnumType")
    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @ManyToOne(targetEntity = Process.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_VARIABLE_PROCESS")
    @Index(name = "IX_VARIABLE_PROCESS")
    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
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

    private VariableLog getLog(Object oldValue, Object newValue, VariableDefinition variableDefinition) {
        if (oldValue == null) {
            return new VariableCreateLog(this, newValue, variableDefinition);
        } else if (newValue == null) {
            return new VariableDeleteLog(this);
        } else {
            return new VariableUpdateLog(this, oldValue, newValue, variableDefinition);
        }
    }

    public boolean supports(Object value) {
        if (value == null) {
            return false;
        }
        return converter != null && converter.supports(value);
    }

    public VariableLog setValue(ExecutionContext executionContext, Object newValue, VariableDefinition variableDefinition) {
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
        setStorableValue((T) newStorableValue);
        return getLog(oldValue, newValue, variableDefinition);
    }

    @Transient
    public Object getValue() {
        Object value = getStorableValue();
        if (value != null && converter != null) {
            value = converter.revert(value);
        }
        return value;
    }

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
        return Objects.toStringHelper(this).add("id", getId()).add("name", getName()).toString();
    }

}
