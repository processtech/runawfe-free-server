package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedList<T> {
    private Integer total;
    private List<T> data;
}
