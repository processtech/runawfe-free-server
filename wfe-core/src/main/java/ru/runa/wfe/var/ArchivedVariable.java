package ru.runa.wfe.var;

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
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import ru.runa.wfe.execution.ArchivedProcess;

@Entity
@Table(name = "ARCHIVED_VARIABLE", uniqueConstraints = { @UniqueConstraint(name = "UK_ARCH_VARIABLE_PROCESS", columnNames = { "PROCESS_ID", "NAME" }) })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
public abstract class ArchivedVariable<T> extends BaseVariable<ArchivedProcess, T> {

    protected Long id;
    private String name;
    private ArchivedProcess process;

    /**
     * NOT generated, id values are preserved when moving row to archive.
     */
    @Override
    @Id
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    @Override
    protected void setId(Long id) {
        this.id = id;
    }

    /**
     * Copy-pasted from Variable with different index name.
     */
    @Override
    @Column(name = "NAME", length = 1024)
    @Index(name = "IX_ARCH_VARIABLE_NAME")
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Copy-pasted from Variable with referenced ArchivedProcess FK and index names.
     */
    @Override
    @ManyToOne(targetEntity = ArchivedProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", nullable = false)
    @ForeignKey(name = "FK_ARCH_VARIABLE_PROCESS")
    @Index(name = "IX_ARCH_VARIABLE_PROCESS")
    public ArchivedProcess getProcess() {
        return process;
    }

    @Override
    public void setProcess(ArchivedProcess process) {
        this.process = process;
    }
}
