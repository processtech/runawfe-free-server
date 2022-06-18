package ru.runa.wfe.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.runa.wfe.var.file.FileVariable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WfeFileVariable implements FileVariable {
    private static final long serialVersionUID = 1L;
    private String name;
    private String contentType;
    private byte[] data;
    private String stringValue;

}
