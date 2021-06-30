package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class PagedList<T> {
    private final Integer total;
    private final List<T> data;
}
