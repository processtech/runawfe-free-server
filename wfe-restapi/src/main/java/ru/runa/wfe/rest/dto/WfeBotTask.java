package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;

import ru.runa.wfe.rest.dto.WfeBot;

@Data
public class WfeBotTask {
    private Long id;
    private Long version;
    private WfeBot botId;
    private String name;
    private String taskHandlerClassName;
    private byte[] configuration;
    private byte[] embeddedFile;
    private String embeddedFileName;
    private Date createDate;
    private Boolean sequentialExecution = Boolean.FALSE;
}
