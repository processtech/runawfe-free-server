package ru.runa.wfe.user;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;
import ru.runa.wfe.security.IdBasedSecuredObject;
import ru.runa.wfe.user.jaxb.ExecutorAdapter;

/*
 * Created on 01.07.2004
 */
/**
 * Represents an Executor. Executor is an abstract object of system that could perform different actions.
 * 
 */
@Entity
@Polymorphism(type = PolymorphismType.EXPLICIT)
@Table(name = "EXECUTOR")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING, length = 1)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlJavaTypeAdapter(ExecutorAdapter.class)
public abstract class Executor extends IdBasedSecuredObject implements Comparable<Executor> {
    private static final long serialVersionUID = 1L;

    public static final String UNAUTHORIZED_EXECUTOR_NAME = "__unauthorized__";

    private Long id;
    private Long version;
    private String name;
    private String description;
    private String fullName;
    private Date createDate;

    protected Executor() {
    }

    protected Executor(String name, String description, String fullName) {
        Preconditions.checkNotNull(name, "name");
        setName(name);
        setDescription(description);
        Preconditions.checkNotNull(fullName, "fullName");
        setFullName(fullName);
        this.createDate = new Date();
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_EXECUTOR", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME", unique = true, nullable = false, length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIPTION", length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Version
    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Column(name = "FULL_NAME", nullable = false, length = 1024)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Executor)) {
            return false;
        }
        Executor executor = (Executor) obj;
        return Objects.equal(getName(), executor.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).toString();
    }

    /**
     * @deprecated use `getFullName()` directly.
     *
     */
    @Transient
    @Deprecated
    public String getLabel() {
        return getFullName();
    }

    @Transient
    protected String getComparisonValue() {
        return getName();
    }

    @Override
    public final int compareTo(Executor o) {
        if (getComparisonValue() == null || o.getComparisonValue() == null) {
            return -1;
        }
        return getComparisonValue().compareTo(o.getComparisonValue());
    }

}
