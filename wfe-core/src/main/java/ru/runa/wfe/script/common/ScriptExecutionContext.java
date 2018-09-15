package ru.runa.wfe.script.common;

import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.logic.ProcessDefinitionLogic;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.report.logic.ReportLogic;
import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.security.logic.AuthorizationLogic;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.user.logic.ProfileLogic;

public class ScriptExecutionContext {

    @Autowired
    ExecutorLogic executorLogic;
    @Autowired
    private RelationLogic relationLogic;
    @Autowired
    private AuthorizationLogic authorizationLogic;
    @Autowired
    private ProcessDefinitionLogic processDefinitionLogic;
    @Autowired
    private ExecutionLogic executionLogic;
    @Autowired
    private ProfileLogic profileLogic;
    @Autowired
    private SubstitutionLogic substitutionLogic;
    @Autowired
    private BotLogic botLogic;
    @Autowired
    private ReportLogic reportLogic;

    private final Map<NamedIdentityType, Map<String, Set<String>>> namedIdentitySets = Maps.newHashMap();

    private User user;

    private String defaultPassword;

    private Map<String, byte[]> externalResources;

    public ScriptExecutionContext() {
        super();
        for (NamedIdentityType type : NamedIdentityType.values()) {
            namedIdentitySets.put(type, new HashMap<>());
        }
    }

    public Set<String> getNamedIdentities(NamedIdentityType type, String name) {
        Set<String> stored = namedIdentitySets.get(type).get(name);
        if (stored == null) {
            throw new AdminScriptException("No named identity set with name '" + name + "' is defined for type " + type.getScriptName());
        }
        return stored;
    }

    public void registerNamedIdentities(NamedIdentityType type, String name, Set<String> set) {
        if (namedIdentitySets.get(type).containsKey(name)) {
            throw new AdminScriptException("Named identity set with name '" + name + "' is already defined for type " + type.getScriptName());
        }
        namedIdentitySets.get(type).put(name, set);
    }

    public ExecutorLogic getExecutorLogic() {
        return executorLogic;
    }

    public RelationLogic getRelationLogic() {
        return relationLogic;
    }

    public AuthorizationLogic getAuthorizationLogic() {
        return authorizationLogic;
    }

    public ProcessDefinitionLogic getProcessDefinitionLogic() {
        return processDefinitionLogic;
    }

    public ExecutionLogic getExecutionLogic() {
        return executionLogic;
    }

    public ProfileLogic getProfileLogic() {
        return profileLogic;
    }

    public SubstitutionLogic getSubstitutionLogic() {
        return substitutionLogic;
    }

    public BotLogic getBotLogic() {
        return botLogic;
    }

    public ReportLogic getReportLogic() {
        return reportLogic;
    }

    public User getUser() {
        return user;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public byte[] getExternalResource(String resourceFile) {
        byte[] externalResource = externalResources.get(resourceFile);
        if (externalResource == null) {
            throw new AdminScriptException("failed to find external resource " + resourceFile);
        }
        return externalResource;
    }

    private void setUser(User user) {
        this.user = user;
    }

    private void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    private void setExternalResources(Map<String, byte[]> externalResources) {
        this.externalResources = externalResources;
    }

    public static ScriptExecutionContext create(User user, Map<String, byte[]> externalResources, String defaultPassword) {
        ScriptExecutionContext context = ApplicationContextFactory.createAutowiredBean(ScriptExecutionContext.class);
        context.setDefaultPassword(defaultPassword);
        context.setUser(user);
        context.setExternalResources(externalResources);
        return context;
    }
}
