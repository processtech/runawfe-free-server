package ru.runa.wfe.rest.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionFileDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.definition.update.validator.ProcessDefinitionNotCompatibleException;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.presentation.filter.FilterFormatException;
import ru.runa.wfe.relation.RelationAlreadyExistException;
import ru.runa.wfe.relation.RelationDoesNotExistException;
import ru.runa.wfe.rest.dto.ExceptionType;
import ru.runa.wfe.rest.dto.WfeExceptionResponse;
import ru.runa.wfe.rest.dto.WfeValidationExceptionResponse;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthenticationExpiredException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.WeakPasswordException;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.task.TaskAlreadyAcceptedException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.ExecutorParticipatesInProcessesException;
import ru.runa.wfe.validation.ValidationException;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {
    public static final List<Class<? extends InternalApplicationException>> WARN_EXCEPTION_CLASSES = Arrays.asList(
            AuthenticationExpiredException.class,
            AuthenticationException.class,
            AuthorizationException.class,
            ExecutorDoesNotExistException.class,
            ExecutorAlreadyExistsException.class,
            ValidationException.class,
            TaskDoesNotExistException.class,
            DefinitionDoesNotExistException.class
    );
    public static final Map<Class<? extends Throwable>, ExceptionType> EXCEPTION_TYPES = new HashMap<Class<? extends Throwable>, ExceptionType>() {{
                put(AuthenticationExpiredException.class, ExceptionType.AUTHENTICATION_EXPIRED);
                put(AuthenticationException.class, ExceptionType.AUTHENTICATION);
                put(AuthorizationException.class, ExceptionType.AUTHORIZATION);
                put(WeakPasswordException.class, ExceptionType.WEAK_PASSWORD);
                put(ExecutorDoesNotExistException.class, ExceptionType.EXECUTOR_DOES_NOT_EXIST);
                put(ExecutorAlreadyExistsException.class, ExceptionType.EXECUTOR_ALREADY_EXISTS);
                put(ExecutorParticipatesInProcessesException.class, ExceptionType.EXECUTOR_PARTICIPATES_IN_PROCESSES);
                put(ProcessDoesNotExistException.class, ExceptionType.PROCESS_DOES_NOT_EXIST);
                put(DefinitionAlreadyExistException.class, ExceptionType.DEFINITION_ALREADY_EXISTS);
                put(DefinitionDoesNotExistException.class, ExceptionType.DEFINITION_DOES_NOT_EXIST);
                put(DefinitionFileDoesNotExistException.class, ExceptionType.DEFINITION_FILE_DOES_NOT_EXIST);
                put(DefinitionArchiveFormatException.class, ExceptionType.DEFINITION_ARCHIVE_FORMAT);
                put(InvalidDefinitionException.class, ExceptionType.INVALID_DEFINITION);
                put(DefinitionNameMismatchException.class, ExceptionType.DEFINITION_NAME_MISMATCH);
                put(ProcessDefinitionNotCompatibleException.class, ExceptionType.DEFINITION_NOT_COMPATIBLE);
                put(TaskDoesNotExistException.class, ExceptionType.TASK_DOES_NOT_EXIST);
                put(TaskAlreadyAcceptedException.class, ExceptionType.TASK_ALREADY_ACCEPTED);
                put(SubstitutionDoesNotExistException.class, ExceptionType.SUBSTITUTION_DOES_NOT_EXIST);
                put(FilterFormatException.class, ExceptionType.FILTER_FORMAT);
                put(ParentProcessExistsException.class, ExceptionType.PARENT_PROCESS_EXISTS);
                put(RelationDoesNotExistException.class, ExceptionType.RELATION_DOES_NOT_EXIST);
                put(RelationAlreadyExistException.class, ExceptionType.RELATION_ALREADY_EXISTS);
                put(ValidationException.class, ExceptionType.VALIDATION);
            }};

    @ExceptionHandler({ AuthenticationException.class })
    protected ResponseEntity<Object> onError(AuthenticationException exception, HttpServletResponse response) {
        log.warn(exception.toString());
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({ ValidationException.class })
    protected ResponseEntity<WfeValidationExceptionResponse> onError(ValidationException exception, HttpServletResponse response) {
        log.warn(exception.toString());
        return new ResponseEntity<>(new WfeValidationExceptionResponse(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ Throwable.class })
    protected ResponseEntity<WfeExceptionResponse> onError(Throwable throwable, HttpServletRequest request, HttpServletResponse response) {
        throwable = Throwables.getRootCause(throwable);
        logException(request, throwable);
        ExceptionType exceptionType = Optional.ofNullable(EXCEPTION_TYPES.get(throwable.getClass())).orElse(ExceptionType.OTHER);
        return new ResponseEntity<>(new WfeExceptionResponse(exceptionType, throwable.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
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
        if (WARN_EXCEPTION_CLASSES.contains(throwable.getClass())) {
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

    public static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                org.springframework.security.core.AuthenticationException authException)
                throws IOException, ServletException {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
    }
}
