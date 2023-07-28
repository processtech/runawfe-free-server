package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;

@Data
public class WfeProcessDefinition {
    private Long id;
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
    private WfeUser createUser;
    private Date updateDate;
    private WfeUser updateUser;
    private Date subprocessBindingDate;
    private Integer secondsBeforeArchiving;
}
