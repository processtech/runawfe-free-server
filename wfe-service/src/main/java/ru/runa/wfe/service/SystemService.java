package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.SystemError;
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
     * Logins to the system. Acquires {@link Permission#LOGIN_TO_SYSTEM} permission.
     * 
     * @param user
     * @throws AuthorizationException
     */
    void login(User user) throws AuthorizationException;

    /**
     * Get currently registered localizations from database.
     */
    List<Localization> getLocalizations();

    /**
     * Get localization of string from database.
     * 
     * @param name
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
     * Get all process errors.
     */
    List<ProcessError> getAllProcessErrors(User user);

    /**
     * Get process errors.
     */
    List<ProcessError> getProcessErrors(User user, Long processId);

    /**
     * Get system errors.
     */
    List<SystemError> getSystemErrors(User user);

}
