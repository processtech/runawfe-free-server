package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WfeExceptionResponse {
    private ExceptionType type;
    private String message;
    private Date date;

    public WfeExceptionResponse(ExceptionType type, String message) {
        this.type = type;
        this.message = message;
        this.date = new Date();
    }
}
