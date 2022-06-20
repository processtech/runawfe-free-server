package ru.runa.wfe.rest.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    public static final List<Class<? extends InternalApplicationException>> warnExceptionClasses = Arrays.asList(
            AuthenticationExpiredException.class,
            AuthenticationException.class, 
            AuthorizationException.class, 
            ExecutorDoesNotExistException.class, 
            ExecutorAlreadyExistsException.class,
            ValidationException.class,
            TaskDoesNotExistException.class, 
            DefinitionDoesNotExistException.class
    );

    @ExceptionHandler({ AuthenticationException.class })
    protected ResponseEntity<Object> onError(AuthenticationException exception, HttpServletResponse response) {
        log.warn(exception.toString());
        return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ ValidationException.class })
    protected ResponseEntity<ValidationExceptionResponse> onError(ValidationException exception, HttpServletResponse response) {
        log.warn(exception.toString());
        return new ResponseEntity<ValidationExceptionResponse>(new ValidationExceptionResponse(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ Throwable.class })
    protected ResponseEntity<ExceptionResponse> onError(Throwable throwable, HttpServletRequest request, HttpServletResponse response) {
        throwable = Throwables.getRootCause(throwable);
        logException(request, throwable);
        return new ResponseEntity<ExceptionResponse>(new ExceptionResponse(throwable), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logException(HttpServletRequest request, Throwable throwable) {
        log.warn(getRequestPath(request));
        Map<String, String> parameters = Maps.transformEntries(request.getParameterMap(), new EntryTransformer<String, String[], String>() {

            @Override
            public String transformEntry(String key, String[] value) {
                return Arrays.toString(value);
            }

        });
        log.warn("Parameters = {}", parameters);
        if (warnExceptionClasses.contains(throwable.getClass())) {
            log.warn(throwable.toString());
        } else {
            log.error("", throwable);
        }
    }

    private String getRequestPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        String queryString = request.getQueryString();
        return (pathInfo != null ? pathInfo : "") + (queryString != null ? "?" + queryString : "");
    }

    @Data
    public class ExceptionResponse {
        private final String exceptionClass;
        private final String message;
        private final Date date = new Date();

        public ExceptionResponse(Throwable throwable) {
            this.exceptionClass = throwable.getClass().getName();
            this.message = throwable.getMessage();
        }
    }

    @Data
    public class ValidationExceptionResponse extends ExceptionResponse {
        private final HashMap<String, List<String>> fieldErrors = new HashMap<>();
        private final List<String> globalErrors = new ArrayList<>();

        public ValidationExceptionResponse(ValidationException exception) {
            super(exception);
            this.fieldErrors.putAll(exception.getFieldErrors());
            this.globalErrors.addAll(exception.getGlobalErrors());
        }

    }

    public static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.core.AuthenticationException authException)
                throws IOException, ServletException {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
    }
}
