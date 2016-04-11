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
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.user.User;

/**
 * Base interface which implementaions provide operations on ASystem they are
 * represent. For example {@link SystemServiceDelegete}represents delegete to
 * AASystem. Created on 17.08.2004
 * 
 * 
 */
public class SystemServiceDelegate extends EJB3Delegate implements SystemService {

    public SystemServiceDelegate() {
        super(SystemService.class);
    }

    private SystemService getSystemService() {
        return getService();
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
    public List<Localization> getLocalizations(User user) {
        try {
            return getSystemService().getLocalizations(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public String getLocalized(User user, String name) {
        try {
            return getSystemService().getLocalized(user, name);
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

}
