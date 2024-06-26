package ru.runa.wfe.definition.dto;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import ru.runa.wfe.commons.Categorized;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.ProcessDefinitionWithContent;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.user.Actor;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class WfDefinition extends SecuredObject implements Comparable<WfDefinition>, Categorized {
    private static final long serialVersionUID = -6032491529439317948L;

    private Long id;
    private Long packId;
    private String name;
    private String description;
    private String category;
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

    public WfDefinition(ProcessDefinitionPack p, ProcessDefinition d) {
        this.id = d.getId();
        this.packId = p.getId();
        this.version = d.getVersion();
        this.name = p.getName();
        this.description = p.getDescription();
        this.category = p.getCategory();
        this.createDate = d.getCreateDate();
        this.createActor = d.getCreateActor();
        this.updateDate = d.getUpdateDate();
        this.updateActor = d.getUpdateActor();
        this.subprocessBindingDate = d.getSubprocessBindingDate();
        this.secondsBeforeArchiving = p.getSecondsBeforeArchiving();
    }

    public WfDefinition(ProcessDefinitionPack p, ProcessDefinitionWithContent d) {
        this.id = d.getId();
        this.packId = p.getId();
        this.version = d.getVersion();
        this.name = p.getName();
        this.description = p.getDescription();
        this.category = p.getCategory();
        this.createDate = d.getCreateDate();
        this.createActor = d.getCreateActor();
        this.updateDate = d.getUpdateDate();
        this.updateActor = d.getUpdateActor();
        this.subprocessBindingDate = d.getSubprocessBindingDate();
        this.secondsBeforeArchiving = p.getSecondsBeforeArchiving();
    }

    public WfDefinition(ProcessDefinition d) {
        this(d.getPack(), d);
    }

    public WfDefinition(ParsedProcessDefinition pd, boolean canBeStarted) {
        this.id = pd.getId();
        this.packId = pd.getPackId();
        this.version = pd.getVersion();
        this.name = pd.getName();
        this.description = pd.getDescription();
        this.category = pd.getCategory();
        this.createDate = pd.getCreateDate();
        this.createActor = pd.getCreateActor();
        this.updateDate = pd.getUpdateDate();
        this.updateActor = pd.getUpdateActor();
        this.subprocessBindingDate = pd.getSubprocessBindingDate();
        this.secondsBeforeArchiving = pd.getSecondsBeforeArchiving();
        this.hasHtmlDescription = pd.getFileData(FileDataProvider.INDEX_FILE_NAME) != null;
        this.hasStartImage = pd.getFileData(FileDataProvider.START_IMAGE_FILE_NAME) != null;
        this.hasDisabledImage = pd.getFileData(FileDataProvider.START_DISABLED_IMAGE_FILE_NAME) != null;
        this.subprocessOnly = pd.getAccessType() == ProcessDefinitionAccessType.OnlySubprocess;
        this.canBeStarted = canBeStarted && !subprocessOnly && pd.getManualStartNode() != null;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DEFINITION;
    }

    @Override
    public Long getSecuredObjectId() {
        return packId;
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

    @Override
    public int compareTo(WfDefinition o) {
        if (name == null) {
            return -1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfDefinition) {
            return Objects.equal(id, ((WfDefinition) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).add("version", version).toString();
    }
}
