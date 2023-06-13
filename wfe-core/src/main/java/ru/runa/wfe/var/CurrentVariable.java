package ru.runa.wfe.var;

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
import ru.runa.wfe.audit.CurrentVariableCreateLog;
import ru.runa.wfe.audit.CurrentVariableDeleteLog;
import ru.runa.wfe.audit.CurrentVariableLog;
import ru.runa.wfe.audit.CurrentVariableUpdateLog;
import ru.runa.wfe.execution.CurrentProcess;

/**
 * Base class for classes that store variable values in the database.
 */
@Entity
@Table(name = "BPM_VARIABLE", uniqueConstraints = { @UniqueConstraint(name = "UK_VARIABLE_PROCESS", columnNames = { "PROCESS_ID", "NAME" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class CurrentVariable<V> extends Variable<CurrentProcess, V> {

    protected Long id;
    private String name;
    private CurrentProcess process;

    public CurrentVariable() {
    }

    @Override
    @Transient
    public boolean isArchived() {
        return false;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_VARIABLE", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    public CurrentProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(CurrentProcess process) {
        this.process = process;
    }

    @Override
    protected CurrentVariableLog getLog(Object oldValue, Object newValue, VariableDefinition variableDefinition) {
        if (oldValue == null) {
            return new CurrentVariableCreateLog(this, newValue, variableDefinition);
        } else if (newValue == null) {
            return new CurrentVariableDeleteLog(this);
        } else {
            return new CurrentVariableUpdateLog(this, newValue, variableDefinition);
        }
    }
}
