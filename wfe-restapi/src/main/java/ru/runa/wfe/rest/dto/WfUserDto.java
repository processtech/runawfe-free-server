package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfUserDto extends ExecutorDto {
    private String description;
    private String title;
    private String email;
    private String phone;
    private String department;
    private Long code;
}
