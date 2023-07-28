package ru.runa.wfe.security.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Executor;

/**
 * Created on 15.12.2004
 */
@Entity
@Table(name = "PRIVELEGED_MAPPING")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class PrivelegedMapping {
    private Long id;
    private SecuredObjectType type;
    private Executor executor;

    protected PrivelegedMapping() {
    }

    public PrivelegedMapping(SecuredObjectType type, Executor executor) {
        setType(type);
        setExecutor(executor);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_PRIVELEGED_MAPPING", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    protected Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = Executor.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "EXECUTOR_ID", nullable = false)
    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    @Column(name = "TYPE", nullable = false, length = 1024)
    @Type(type = "ru.runa.wfe.commons.hibernate.SecuredObjectTypeType")
    public SecuredObjectType getType() {
        return type;
    }

    public void setType(SecuredObjectType type) {
        this.type = type;
    }

}
