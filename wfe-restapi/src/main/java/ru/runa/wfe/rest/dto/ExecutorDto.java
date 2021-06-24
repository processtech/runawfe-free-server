package ru.runa.wfe.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorDto {
    private Long id;
    private String name;
    private String fullName;

}
