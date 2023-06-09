package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;

@Data
public class WfeBotStation {
    private Long id;
    private Long version;
    private String name;
    private Date createDate;
}
