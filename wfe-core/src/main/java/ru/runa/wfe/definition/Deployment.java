package ru.runa.wfe.definition;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "BPM_PROCESS_DEFINITION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Deployment extends DeploymentData {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS_DEFINITION", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id();
    }

    @Transient
    public Deployment getCopy() {
        return from(this);
    }

    @Transient
    public static Deployment from(DeploymentData deploymentData) {
        Deployment deployment = new Deployment();
        deployment.setCategory(deploymentData.getCategory());
        deployment.setCreateDate(deploymentData.getCreateDate());
        deployment.setDescription(deploymentData.getDescription());
        deployment.setId(deploymentData.id());
        deployment.setLanguage(deploymentData.getLanguage());
        deployment.setName(deploymentData.getName());
        deployment.setVersion(deploymentData.getVersion());
        return deployment;
    }

}
