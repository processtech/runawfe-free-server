package ru.runa.wfe.audit;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PDDel")
public class ProcessDefinitionDeleteLog extends SystemLog {
    private String name;
    private Long version;

    protected ProcessDefinitionDeleteLog() {
    }

    public ProcessDefinitionDeleteLog(Long actorId, String name, Long version) {
        super(actorId);
        this.name = name;
        this.version = version;
    }

    @Column(name = "PROCESS_DEFINITION_NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "PROCESS_DEFINITION_VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
