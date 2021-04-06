package ru.runa.wfe.service.delegate;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.user.User;

public class ScriptingServiceDelegate extends Ejb3Delegate implements ScriptingService {

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
