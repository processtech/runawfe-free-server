package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class WfTasksDto {
    private Integer total;
    private List<WfTaskDto> tasks;
}
