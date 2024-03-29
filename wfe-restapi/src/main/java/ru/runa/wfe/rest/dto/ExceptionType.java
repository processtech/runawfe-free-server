package ru.runa.wfe.rest.dto;

public enum ExceptionType {
    AUTHENTICATION_EXPIRED,
    AUTHENTICATION,
    AUTHORIZATION,
    WEAK_PASSWORD,
    EXECUTOR_DOES_NOT_EXIST,
    EXECUTOR_ALREADY_EXISTS,
    EXECUTOR_PARTICIPATES_IN_PROCESSES,
    PROCESS_DOES_NOT_EXIST,
    DEFINITION_ALREADY_EXISTS,
    DEFINITION_DOES_NOT_EXIST,
    DEFINITION_FILE_DOES_NOT_EXIST,
    DEFINITION_ARCHIVE_FORMAT,
    INVALID_DEFINITION,
    DEFINITION_NAME_MISMATCH,
    DEFINITION_NOT_COMPATIBLE,
    TASK_DOES_NOT_EXIST,
    TASK_ALREADY_ACCEPTED,
    SUBSTITUTION_DOES_NOT_EXIST,
    FILTER_FORMAT,
    PARENT_PROCESS_EXISTS,
    RELATION_DOES_NOT_EXIST,
    RELATION_ALREADY_EXISTS,
    VALIDATION,
    OTHER
}
