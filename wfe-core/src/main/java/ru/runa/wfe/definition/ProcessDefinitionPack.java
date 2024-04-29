package ru.runa.wfe.definition;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.security.IdBasedSecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

@Entity
@Table(name = "BPM_PROCESS_DEFINITION_PACK")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProcessDefinitionPack extends IdBasedSecuredObject {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Language language;
    private String description;
    private String category;
    private ProcessDefinition latest;

    /**
     * Seconds, not days -- for easier debugging.
     * If null, use SystemProperties.getProcessDefaultSecondsBeforeArchiving().
     */
    private Integer secondsBeforeArchiving;

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS_DEFINITION_PC", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NAME", nullable = false, length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * "BPMN" или "JPDL".
     */
    @Column(name = "LANGUAGE", nullable = false, length = 4)
    @Enumerated(value = EnumType.STRING)
    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Column(name = "DESCRIPTION", length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Тип процесса. Перезаписывается при redeploy / update.
     */
    @Column(name = "CATEGORY", nullable = false, length = 1024)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * For manually written queries this field is redundant, since there's unique index on bpm_process_definition (definition_id, version).
     * But it's also used in DefinitionClassPresentation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LATEST_DEFINITION_ID")
    public ProcessDefinition getLatest() {
        return latest;
    }

    public void setLatest(ProcessDefinition latestProcessDefinition) {
        this.latest = latestProcessDefinition;
    }

    @Column(name = "SECONDS_BEFORE_ARCHIVING")
    public Integer getSecondsBeforeArchiving() {
        return secondsBeforeArchiving;
    }

    public void setSecondsBeforeArchiving(Integer endedDaysBeforeArchiving) {
        this.secondsBeforeArchiving = endedDaysBeforeArchiving;
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DEFINITION;
    }

    public void setCategories(List<String> categories) {
        category = Joiner.on(Utils.CATEGORY_DELIMITER).join(categories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessDefinitionPack)) {
            return false;
        }
        ProcessDefinitionPack x = (ProcessDefinitionPack) o;
        return Objects.equal(id, x.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

}
