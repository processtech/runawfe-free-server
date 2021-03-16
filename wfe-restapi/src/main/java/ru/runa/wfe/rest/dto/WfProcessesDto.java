package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class WfProcessesDto {
    private Integer total;
    private List<WfProcessDto> processes;
}
