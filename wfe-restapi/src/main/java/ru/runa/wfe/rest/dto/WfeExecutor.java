package ru.runa.wfe.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WfeExecutor {
    private Long id;
    private Type type;
    private String name;
    private String description;
    private String fullName;

    public enum Type {
        USER,
        GROUP,
        TEMPORARY_GROUP,
        DELEGATION_GROUP,
        ESCALATION_GROUP
    }
}
