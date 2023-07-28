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
     * @param externalResources
     *            external script data (par files, bot configurations and so one).
     * @throws AdminScriptException
     *             if script execution fails
     */
    void executeAdminScript(User user, byte[] configData, Map<String, byte[]> externalResources) throws AdminScriptException;

    List<String> executeAdminScriptSkipError(User user, byte[] configData, Map<String, byte[]> externalResources, String defaultPasswordValue);

    List<String> executeAdminScriptSkipError(User user, byte[] configData, Map<String, byte[]> externalResources, String defaultPasswordValue,
            String dataSourceDefaultPasswordValue);

    /**
     * Executes Groovy script.
     *
     * @param user
     *            authorized user
     * @param script
     *            groovy code
     */
    void executeGroovyScript(User user, String script);

    List<String> getScriptsNames();

    void saveScript(String fileName, byte[] script);

    void deleteScript(String fileName);

    byte[] getScriptSource(String fileName);
}
