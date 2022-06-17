package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeUser extends WfeExecutor {
    private String description;
    private String title;
    private String email;
    private String phone;
    private String department;
    private Long code;
}
