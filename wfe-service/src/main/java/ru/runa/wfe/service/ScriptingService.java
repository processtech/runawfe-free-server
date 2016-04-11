package ru.runa.wfe.service;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import ru.runa.wfe.script.AdminScriptException;
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
     * @param processDefinitionsBytes
     *            process definitions data to deploy or update
     * @throws AdminScriptException
     *             if script execution fails
     */
    public void executeAdminScript(User user, byte[] scriptData, byte[][] processDefinitionsBytes) throws AdminScriptException;

    public List<String> executeAdminScriptSkipError(User user, byte[] configData, byte[][] processDefinitionsBytes, Map<String, byte[]> configs, String defaultPasswordValue);

    /**
     * Executes Groovy script.
     * 
     * @param user
     *            authorized user
     * @param script
     *            groovy code
     */
    public void executeGroovyScript(User user, String script);

}
