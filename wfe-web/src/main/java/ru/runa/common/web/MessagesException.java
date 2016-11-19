package ru.runa.common.web;

public final class MessagesException {

    public static final StrutsMessage EXCEPTION_UNKNOWN = new StrutsMessage("unknown.exception");
    public static final StrutsMessage EXCEPTION_SESSION_INVALID = new StrutsMessage("session.invalid");
    public static final StrutsMessage EXCEPTION_TABLE_VIEW_SETUP_FORMAT_INCORRECT = new StrutsMessage("view.setup.format.invalid");
    public static final StrutsMessage EXCEPTION_AUTHORIZATION = new StrutsMessage("authorization.exception");
    public static final StrutsMessage EXCEPTION_AUTHENTICATION = new StrutsMessage("authentication.exception");
    public static final StrutsMessage EXCEPTION_PASSWORD_IS_WEAK = new StrutsMessage("executor.weak.password");
    public static final StrutsMessage EXCEPTION_EXECUTOR_ALREADY_EXISTS = new StrutsMessage("executor.already.exists.exception");
    public static final StrutsMessage EXCEPTION_EXECUTOR_DOES_NOT_EXISTS = new StrutsMessage("executor.does.not.exists.exception");
    public static final StrutsMessage EXCEPTION_ACTOR_DOES_NOT_EXISTS = new StrutsMessage("ru.runa.wf.web.actor.does.not.exists.exception");
    public static final StrutsMessage EXCEPTION_GROUP_DOES_NOT_EXISTS = new StrutsMessage("ru.runa.wf.web.group.does.not.exists.exception");
    public static final StrutsMessage EXCEPTION_EXECUTOR_PARTICIPATES_IN_PROCESSES = new StrutsMessage("executor.participates.in.processes");
    public static final StrutsMessage ERROR_NULL_VALUE = new StrutsMessage("emptyvalue");
    public static final StrutsMessage ERROR_FILL_REQUIRED_VALUES = new StrutsMessage("error.fill.required.values");
    public static final StrutsMessage ERROR_VALIDATION = new StrutsMessage("validation.error");
    public static final StrutsMessage ERROR_PASSWORDS_NOT_MATCH = new StrutsMessage("executor.passwords.not.match");
    public static final StrutsMessage ERROR_DEFINITION_ALREADY_EXISTS = new StrutsMessage("definition.already.exists.error");
    public static final StrutsMessage ERROR_DEFINITION_DOES_NOT_EXIST = new StrutsMessage("definition.does.not.exist.error");
    public static final StrutsMessage ERROR_DEFINITION_NAME_MISMATCH = new StrutsMessage("definition.name.mismatch.error");
    public static final StrutsMessage ERROR_PROCESS_DOES_NOT_EXIST = new StrutsMessage("process.does.not.exist.error");
    public static final StrutsMessage ERROR_TASK_DOES_NOT_EXIST = new StrutsMessage("task.does.not.exist.error");
    public static final StrutsMessage ERROR_TASK_FORM_NOT_DEFINED = new StrutsMessage("task.form.not.defined.error");
    public static final StrutsMessage DEFINITION_ALREADY_LOCKED = new StrutsMessage("definition.already.locked.error");
    public static final StrutsMessage DEFINITION_ARCHIVE_FORMAT_ERROR = new StrutsMessage("definition.archive.format.error");
    public static final StrutsMessage DEFINITION_FILE_FORMAT_ERROR = new StrutsMessage("definition.file.format.error");
    public static final StrutsMessage DEFINITION_FILE_DOES_NOT_EXIST_ERROR = new StrutsMessage("definition.file.does.not.exist.error");
    public static final StrutsMessage DEFINITION_LOCKED = new StrutsMessage("definition.locked.error");
    public static final StrutsMessage DEFINITION_LOCKED_FOR_ALL = new StrutsMessage("definition.locked.for.all.error");
    public static final StrutsMessage SUBSTITUTION_OUT_OF_DATE = new StrutsMessage("substitution.out.of.date.error");
    public static final StrutsMessage TASK_WAS_ALREADY_ACCEPTED = new StrutsMessage("task.was.already.accepted");
    public static final StrutsMessage TASK_WAS_ALREADY_COMPLETED = new StrutsMessage("task.was.already.completed");
    public static final StrutsMessage PROCESS_HAS_SUPER_PROCESS = new StrutsMessage("process.has.super.process");
    public static final StrutsMessage MESSAGE_RELATION_GROUP_EXISTS = new StrutsMessage("label.relation.exists");
    public static final StrutsMessage MESSAGE_RELATION_GROUP_DOESNOT_EXISTS = new StrutsMessage("label.relation.not_exists");
    public static final StrutsMessage MESSAGE_VARIABLE_FORMAT_ERROR = new StrutsMessage("variable.format.error");
    public static final StrutsMessage MESSAGE_VALIDATION_ERROR = new StrutsMessage("validation.form.error");
    public static final StrutsMessage EXCEPTION_DATAFILE_NOT_PRESENT = new StrutsMessage("managesystem.datafile.not.present");
}
