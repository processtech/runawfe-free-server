package ru.runa.wfe.var;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import ru.runa.wfe.audit.CurrentVariableLog;
import ru.runa.wfe.execution.ArchivedProcess;

@Entity
@Table(name = "ARCHIVED_VARIABLE", uniqueConstraints = { @UniqueConstraint(name = "UK_ARCH_VARIABLE_PROCESS", columnNames = { "PROCESS_ID", "NAME" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
public abstract class ArchivedVariable<V> extends Variable<ArchivedProcess, V> {

    protected Long id;
    private String name;
    private ArchivedProcess process;

    @Override
    @Transient
    public boolean isArchived() {
        return true;
    }

    /**
     * NOT generated, id values are preserved when moving row to archive.
     */
    @Override
    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    private void setId(Long id) {
        this.id = id;
    }

    @Override
    protected void setVersion(Long version) {
        this.version = version;
    }

    @Override
    protected void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    protected void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    protected void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    /**
     * Copy-pasted from Variable with different index name.
     */
    @Override
    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    /**
     * Copy-pasted from Variable with referenced ArchivedProcess FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    public ArchivedProcess getProcess() {
        return process;
    }

    protected void setProcess(ArchivedProcess process) {
        this.process = process;
    }

    @Override
    protected CurrentVariableLog getLog(Object oldValue, Object newValue, VariableDefinition variableDefinition) {
        return null;
    }
}
