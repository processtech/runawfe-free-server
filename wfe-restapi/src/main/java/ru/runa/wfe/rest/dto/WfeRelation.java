package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;

@Data
public class WfeRelation {
    private Long id;
    private String name;
    private String description;
    private Date createDate;
}
