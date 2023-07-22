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
package ru.runa.wfe.service.delegate;

import java.util.List;
import ru.runa.wfe.commons.dao.Localization;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.commons.error.dto.WfTokenError;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.user.User;

public class SystemServiceDelegate extends Ejb3Delegate implements SystemService {

    public SystemServiceDelegate() {
        super(SystemService.class);
    }

    private SystemService getSystemService() {
        return getService();
    }

    @Override
    public void initialize() {
        try {
            getSystemService().initialize();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void login(User user) {
        try {
            getSystemService().login(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Localization> getLocalizations() {
        try {
            return getSystemService().getLocalizations();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public String getLocalized(String name) {
        try {
            return getSystemService().getLocalized(name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void saveLocalizations(User user, List<Localization> localizations) {
        try {
            getSystemService().saveLocalizations(user, localizations);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public String getSetting(String fileName, String name) {
        try {
            return getSystemService().getSetting(fileName, name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void setSetting(String fileName, String name, String value) {
        try {
            getSystemService().setSetting(fileName, name, value);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void clearSettings() {
        try {
            getSystemService().clearSettings();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void failToken(User user, Long tokenId, String errorMessage, String stackTrace) {
        try {
            getSystemService().failToken(user, tokenId, errorMessage, stackTrace);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeTokenError(User user, Long tokenId) {
        try {
            getSystemService().removeTokenError(user, tokenId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfTokenError> getTokenErrors(User user, BatchPresentation batchPresentation) {
        try {
            return getSystemService().getTokenErrors(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public int getTokenErrorsCount(User user, BatchPresentation batchPresentation) {
        try {
            return getSystemService().getTokenErrorsCount(user, batchPresentation);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<WfTokenError> getTokenErrorsByProcessId(User user, Long processId) {
        try {
            return getSystemService().getTokenErrorsByProcessId(user, processId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public WfTokenError getTokenError(User user, Long tokenId) {
        try {
            return getSystemService().getTokenError(user, tokenId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<SystemError> getSystemErrors(User user) {
        try {
            return getSystemService().getSystemErrors(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] exportDataFile(User user) {
        try {
            return getSystemService().exportDataFile(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isPasswordCheckRequired() {
        try {
            return getSystemService().isPasswordCheckRequired();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
