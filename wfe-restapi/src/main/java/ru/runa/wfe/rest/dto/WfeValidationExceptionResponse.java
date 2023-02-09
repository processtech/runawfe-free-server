package ru.runa.wfe.rest.dto;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.runa.wfe.validation.ValidationException;

@Data
@NoArgsConstructor
public class WfeValidationExceptionResponse extends WfeExceptionResponse {
    private Map<String, List<String>> fieldErrors;
    private Collection<String> globalErrors;

    public WfeValidationExceptionResponse(ValidationException exception) {
        super(ExceptionType.VALIDATION, exception.getMessage());
        this.fieldErrors = exception.getFieldErrors();
        this.globalErrors = exception.getGlobalErrors();
    }
}
