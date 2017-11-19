package ru.runa.wfe.definition;

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

/**
 *
 * If use in one transaction with {@link DeploymentContent} for the same row use {@link DeploymentContent} only.
 *
 * */
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
        deployment.setSubprocessBindingDate(deploymentData.getSubprocessBindingDate());
        deployment.setVersion(deploymentData.getVersion());
        return deployment;
    }

}
