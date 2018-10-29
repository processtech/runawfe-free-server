package ru.runa.wfe.definition.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.commons.EntityWithType;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.definition.ProcessDefinitionVersion;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Actor;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfDefinition extends SecuredObject implements Comparable<WfDefinition>, EntityWithType {
    private static final long serialVersionUID = -6032491529439317948L;

    private Long id;
    private Long versionId;
    private String name;
    private String description;
    private String[] categories;
    private Long version;
    private boolean hasHtmlDescription;
    private boolean hasStartImage;
    private boolean hasDisabledImage;
    private boolean subprocessOnly;
    private boolean canBeStarted;
    private Date createDate;
    private Actor createActor;
    private Date updateDate;
    private Actor updateActor;
    private Date subprocessBindingDate;
    private Integer secondsBeforeArchiving;

    public WfDefinition() {
    }

    public WfDefinition(ProcessDefinition d, ProcessDefinitionVersion dv) {
        id = d.getId();
        versionId = dv.getId();
        version = dv.getVersion();
        name = d.getName();
        description = d.getDescription();
        categories = d.getCategories();
        createDate = dv.getCreateDate();
        createActor = dv.getCreateActor();
        updateDate = dv.getUpdateDate();
        updateActor = dv.getUpdateActor();
        subprocessBindingDate = dv.getSubprocessBindingDate();
        secondsBeforeArchiving = d.getSecondsBeforeArchiving();
    }

    public WfDefinition(ProcessDefinitionWithVersion dwv) {
        this(dwv.processDefinition, dwv.processDefinitionVersion);
    }

    public WfDefinition(ProcessDefinitionVersion dv) {
        this(dv.getDefinition(), dv);
    }

    public WfDefinition(ParsedProcessDefinition pd, boolean canBeStarted) {
        this(pd.getProcessDefinition(), pd.getProcessDefinitionVersion());
        hasHtmlDescription = pd.getFileData(FileDataProvider.INDEX_FILE_NAME) != null;
        hasStartImage = pd.getFileData(FileDataProvider.START_IMAGE_FILE_NAME) != null;
        hasDisabledImage = pd.getFileData(FileDataProvider.START_DISABLED_IMAGE_FILE_NAME) != null;
        subprocessOnly = pd.getAccessType() == ProcessDefinitionAccessType.OnlySubprocess;
        this.canBeStarted = canBeStarted && !subprocessOnly;
    }

    @Override
    public Long getIdentifiableId() {
        return (long) getName().hashCode();
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DEFINITION;
    }

    public Long getId() {
        return id;
    }

    public Long getVersionId() {
        return versionId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public String[] getCategories() {
        return categories;
    }

    public boolean hasHtmlDescription() {
        return hasHtmlDescription;
    }

    public boolean hasStartImage() {
        return hasStartImage;
    }

    public boolean hasDisabledImage() {
        return hasDisabledImage;
    }

    public boolean isSubprocessOnly() {
        return subprocessOnly;
    }

    public boolean isCanBeStarted() {
        return canBeStarted;
    }

    public void setCanBeStarted(boolean canBeStarted) {
        this.canBeStarted = canBeStarted;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Actor getCreateActor() {
        return createActor;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public Actor getUpdateActor() {
        return updateActor;
    }

    public Date getSubprocessBindingDate() {
        return subprocessBindingDate;
    }

    public Integer getSecondsBeforeArchiving() {
        return secondsBeforeArchiving;
    }

    @Override
    public int compareTo(WfDefinition o) {
        if (name == null) {
            return -1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(versionId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfDefinition) {
            return Objects.equal(versionId, ((WfDefinition) obj).versionId);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("versionId", versionId).add("name", name).add("version", version).toString();
    }
}
