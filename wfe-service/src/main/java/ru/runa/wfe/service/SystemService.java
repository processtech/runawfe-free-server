/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.error.ProcessError;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.SystemPermission;
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
    public void initialize();

    /**
     * Logins to the system. Acquires {@link SystemPermission#LOGIN_TO_SYSTEM} permission.
     * 
     * @param user
     * @throws AuthorizationException
     */
    public void login(User user) throws AuthorizationException;

    /**
     * Get currently registered localizations from database.
     * 
     * @return
     */
    public List<Localization> getLocalizations();

    /**
     * Get localization of string from database.
     * 
     * @param name
     * 
     * @return localized string
     */
    public String getLocalized(String name);

    /**
     * Update localizations in database.
     * 
     * @param user
     * @param localizations
     */
    public void saveLocalizations(User user, List<Localization> localizations);

    /**
     * Get property value with key (fileName, name) from database
     * 
     * @param fileName
     * @param name
     * @return
     */
    public String getSetting(String fileName, String name);

    /**
     * Get property value with key (fileName, name) in database
     * 
     * @param fileName
     * @param name
     * @param value
     */
    public void setSetting(String fileName, String name, String value);

    /**
     * Remove all properties from database
     */
    public void clearSettings();

    /**
     * Get all process errors.
     */
    public List<ProcessError> getAllProcessErrors(User user);

    /**
     * Get process errors.
     */
    public List<ProcessError> getProcessErrors(User user, Long processId);

    /**
     * Get system errors.
     */
    public List<SystemError> getSystemErrors(User user);

}
