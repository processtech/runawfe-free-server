package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.User;

/**
 * Service for common operations.
 * 
 * @since 2.0
 */
public interface SystemService {

    /**
     * Performs application context startup.
     */
    void initialize();

    /**
     * Logins to the system. Requires {@link Permission#LOGIN} permission.
     */
    void login(User user) throws AuthorizationException;

    /**
     * Get currently registered localizations from database.
     */
    List<Localization> getLocalizations();

    /**
     * Get localization of string from database.
     * 
     * @return localized string
     */
    String getLocalized(String name);

    /**
     * Update localizations in database.
     */
    void saveLocalizations(User user, List<Localization> localizations);

    /**
     * Get property value with key (fileName, name) from database
     */
    String getSetting(String fileName, String name);

    /**
     * Get property value with key (fileName, name) in database.
     */
    void setSetting(String fileName, String name, String value);

    /**
     * Remove all properties from database.
     */
    void clearSettings();

    /**
     * Change token's execution status and set error.
     */
    void failToken(User user, Long tokenId, String errorMessage, String stackTrace);

    /**
     * Remove token error without changing its execution status.
     */
    void removeTokenError(User user, Long tokenId);

    /**
     * Get token errors.
     */
    List<WfTokenError> getTokenErrors(User user, BatchPresentation batchPresentation);

    /**
     * Get token errors without stack trace.
     */
    String getTokenErrorStackTrace(User user, Long tokenId);

    /**
     * Get token errors count.
     */
    int getTokenErrorsCount(User user, BatchPresentation batchPresentation);

    /**
     * Get token errors by processId.
     */
    List<WfTokenError> getTokenErrorsByProcessId(User user, Long processId);

    /**
     * Get system errors.
     */
    List<SystemError> getSystemErrors(User user);

    /**
     * Export system data file
     */
    byte[] exportDataFile(User user);

    boolean isPasswordCheckRequired();

}
