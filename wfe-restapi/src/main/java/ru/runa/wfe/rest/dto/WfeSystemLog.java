package ru.runa.wfe.rest.dto;

import lombok.Data;
import java.util.Date;

@Data
public class WfeSystemLog {
    private Long id;
    private Long actorId;
    private Date createDate;
}
