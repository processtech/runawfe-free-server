package ru.runa.wf.logic.bot.mswordreport;

import ru.runa.wfe.LocalizableException;

public class MsWordReportException extends LocalizableException {
    private static final long serialVersionUID = 1L;
    public static final String MSWORD_APP_COMM_ERROR = "error.msword.application.communication";
    public static final String OPEN_TEMPLATE_DOCUMENT_FAILED = "error.msword.open.template.document";
    public static final String BOOKMARK_NOT_FOUND_IN_TEMPLATE = "error.msword.bookmark.missed";
    public static final String VARIABLE_NOT_FOUND_IN_PROCESS = "error.msword.variable.missed";
    public static final String TEMPLATE_NOT_FOUND = "error.msword.template.notfound";

    public MsWordReportException(String message, Object... details) {
        super(message, details);
    }

    @Override
    protected String getResourceBaseName() {
        return "bot.error";
    }
}
