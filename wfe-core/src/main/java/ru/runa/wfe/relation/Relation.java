package ru.runa.wfe.relation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

/**
 * Relation between executors. Each relation contains some RelationPair, which
 * describe executors relation.
 */
@Entity
@Table(name = "EXECUTOR_RELATION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Relation extends SecuredObject {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Date createDate;

    public Relation() {
    }

    /**
     * Create instance of relation with given name and description.
     * 
     * @param name
     *            Name of relation.
     * @param description
     *            Description of relation.
     */
    public Relation(String name, String description) {
        this.name = name;
        this.description = description;
        this.createDate = new Date();
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.RELATION;
    }

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_EXECUTOR_RELATION", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Return name of relation.
     * 
     * @return Name of relation.
     */
    @Column(name = "NAME", unique = true, length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return description of relation.
     * 
     * @return Description of relation.
     */
    @Column(name = "DESCRIPTION", length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Relation other = (Relation) obj;
        return Objects.equal(name, other.name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName()).toString();
    }
}
