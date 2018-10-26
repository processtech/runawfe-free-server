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
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

@Entity
@Table(name = "BPM_PROCESS_DEFINITION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ProcessDefinition extends SecuredObject {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private Language language;
    private String description;
    private String category;
    private ProcessDefinitionVersion latestVersion;

    /**
     * Seconds, not days -- for easier debugging.
     * If null, use SystemProperties.getProcessDefaultSecondsBeforeArchiving().
     */
    private Integer secondsBeforeArchiving;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_PROCESS_DEFINITION", allocationSize = 1)
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
     * For manually written queries this field is redundant, since there's unique index on bpm_process_definition_ver (definition_id, version).
     * But it's also used in DefinitionClassPresentation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LATEST_VERSION_ID")
    public ProcessDefinitionVersion getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(ProcessDefinitionVersion latestVersion) {
        this.latestVersion = latestVersion;
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
    public Long getIdentifiableId() {
        return getId();
    }

    @Transient
    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DEFINITION;
    }

    @Transient
    public String[] getCategories() {
        if (category != null) {
            return category.split(Utils.CATEGORY_DELIMITER);
        }
        return new String[] {};
    }

    public void setCategories(List<String> categories) {
        category = Joiner.on(Utils.CATEGORY_DELIMITER).join(categories);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessDefinition)) return false;
        ProcessDefinition x = (ProcessDefinition) o;
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

    @Transient
    public ProcessDefinition createCopy() {
        ProcessDefinition o = new ProcessDefinition();
        o.id = id;
        o.name = name;
        o.language = language;
        o.description = description;
        o.category = category;
        return o;
    }
}
