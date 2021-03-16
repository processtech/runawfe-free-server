package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;

@Data
public class WfProcessDto {
    private Long id;
    private String name;
    private String executionStatus;
    private Date startDate;
    private Date endDate; 
}
