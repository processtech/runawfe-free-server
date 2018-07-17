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
        return executeAdminScriptSkipError(user, configData, externalResources, defaultPasswordValue, null);
    }

    @Override
    public List<String> executeAdminScriptSkipError(User user, byte[] configData, Map<String, byte[]> externalResources, String defaultPasswordValue,
            String dataSourceDefaultPasswordValue) {
        try {
            return getScriptingService().executeAdminScriptSkipError(user, configData, externalResources, defaultPasswordValue,
                    dataSourceDefaultPasswordValue);
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
    public List<String> getScriptsNames() {
        try {
            return getScriptingService().getScriptsNames();
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

    @Override
    public void deleteScript(String fileName) {
        try {
            getScriptingService().deleteScript(fileName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] getScriptSource(String fileName) {
        try {
            return getScriptingService().getScriptSource(fileName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
