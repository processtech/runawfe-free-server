package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeTransition {
    private String id;
    private String name;
    private String description;
    private String nodeFromId;
    private String nodeToId;
    private String color;

}
