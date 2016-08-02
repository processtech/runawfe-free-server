package ru.runa.wfe.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.script.dto.AdminScript;
import ru.runa.wfe.user.User;

/**
 * Service for remote script execution.
 * 
 * @author dofs
 * @since 4.0.4
 */
@Remote
public interface ScriptingService {
    /**
     * Executes administrative XML-based script.
     * 
     * @param user
     *            authorized user
     * @param scriptData
     *            script data
     * @param externalResources
     *            external script data (par files, bot configurations and so one).
     * @throws AdminScriptException
     *             if script execution fails
     */
    public void executeAdminScript(User user, byte[] configData, Map<String, byte[]> externalResources) throws AdminScriptException;

    public List<String> executeAdminScriptSkipError(User user, byte[] configData, Map<String, byte[]> externalResources,
            String defaultPasswordValue);

    /**
     * Executes Groovy script.
     * 
     * @param user
     *            authorized user
     * @param script
     *            groovy code
     */
    public void executeGroovyScript(User user, String script);

    public List<AdminScript> getScripts();

    public void saveScript(String fileName, byte[] script);

    public boolean deleteScript(String fileName);

    public byte[] getScriptSource(String fileName);
}
