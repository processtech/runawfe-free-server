package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;

@Data
public class WfDefinitionDto {
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
    private WfUserDto createActor;
    private Date updateDate;
    private WfUserDto updateActor;
    private Date subprocessBindingDate;
    private Integer secondsBeforeArchiving;
}
