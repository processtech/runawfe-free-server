package ru.runa.wfe.definition;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

/**
 * @author Egor Litvinenko
 * @since 13.03.17
 */
@Entity
@Table(name = "BPM_PROCESS_DEFINITION")
@Cache(usage = CacheConcurrencyStrategy.NONE)
@Cacheable(false)
public class DeploymentContent extends DeploymentData {

    private byte[] content;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS_DEFINITION", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id();
    }


    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(length = 16777216, name = "BYTES")
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
