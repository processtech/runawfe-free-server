package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class WfeReport {
    private Long id;
    private Long version;
    private String name;
    private String description;
    private String category;
    private List<WfeReportParameter> parameters = new ArrayList<>();
}
