package ru.runa.wfe.rest.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.Data;

import ru.runa.wfe.rest.dto.WfeBotStation;

@Data
public class WfeBot {
    private Long id;
    private Long version;
    private WfeBotStation botStation;
    private String username;
    private Date createDate;
    private Boolean sequentialExecution = Boolean.FALSE;
    private Boolean transactional = Boolean.FALSE;
    private Long transactionalTimeout;
    private Date boundDueDate;
    private Long boundProcessId;
    private String boundSubprocessId;
}
