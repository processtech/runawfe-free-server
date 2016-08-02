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
import java.util.Map;

import ru.runa.wfe.script.dto.AdminScript;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.user.User;

public class ScriptingServiceDelegate extends EJB3Delegate implements ScriptingService {

    public ScriptingServiceDelegate() {
        super("ScriptingServiceBean", ScriptingService.class);
    }

    private ScriptingService getScriptingService() {
        return getService();
    }

    @Override
    public List<String> executeAdminScriptSkipError(User user, byte[] configData, Map<String, byte[]> externalResources,
            String defaultPasswordValue) {
        try {
            return getScriptingService().executeAdminScriptSkipError(user, configData, externalResources, defaultPasswordValue);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void executeAdminScript(User user, byte[] configData, Map<String, byte[]> externalResources) {
        try {
            getScriptingService().executeAdminScript(user, configData, externalResources);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void executeGroovyScript(User user, String script) {
        try {
            getScriptingService().executeGroovyScript(user, script);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<AdminScript> getScripts() {
        try {
            return getScriptingService().getScripts();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void saveScript(String fileName, byte[] script) {
        try {
            getScriptingService().saveScript(fileName, script);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
