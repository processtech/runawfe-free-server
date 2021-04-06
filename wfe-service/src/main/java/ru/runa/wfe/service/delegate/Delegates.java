package ru.runa.wfe.service.delegate;

import com.google.common.collect.Maps;
import java.util.Map;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.service.AuditService;
import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.service.AuthorizationService;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.ChatService;
import ru.runa.wfe.service.DataSourceService;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.ExecutionService;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.ProfileService;
import ru.runa.wfe.service.RelationService;
import ru.runa.wfe.service.ReportService;
import ru.runa.wfe.service.ScriptingService;
import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.service.SynchronizationService;
import ru.runa.wfe.service.SystemService;
import ru.runa.wfe.service.TaskService;

/**
 * Provides delegates. Delegate's type can not be switched at run-time.
 * 
 * @since 4.0
 */
public class Delegates {
    private static Map<Class<?>, Object> delegates = Maps.newHashMap();

    @SuppressWarnings("unchecked")
    protected static <T extends Ejb3Delegate> T getDelegate(Class<T> delegateClass) {
        if (!delegates.containsKey(delegateClass)) {
            Object delegate = createDelegate(delegateClass);
            delegates.put(delegateClass, delegate);
        }
        return (T) delegates.get(delegateClass);
    }

    public static <T extends Ejb3Delegate> T createDelegate(Class<T> delegateClass) {
        return ClassLoaderUtil.instantiate(delegateClass);
    }
    
    public static ChatService getChatService() {
        return getDelegate(ChatServiceDelegate.class);
    }

    public static AuditService getAuditService() {
        return getDelegate(AuditServiceDelegate.class);
    }

    public static AuthenticationService getAuthenticationService() {
        return getDelegate(AuthenticationServiceDelegate.class);
    }

    public static AuthorizationService getAuthorizationService() {
        return getDelegate(AuthorizationServiceDelegate.class);
    }

    public static ExecutorService getExecutorService() {
        return getDelegate(ExecutorServiceDelegate.class);
    }

    public static RelationService getRelationService() {
        return getDelegate(RelationServiceDelegate.class);
    }

    public static SystemService getSystemService() {
        return getDelegate(SystemServiceDelegate.class);
    }

    public static ProfileService getProfileService() {
        return getDelegate(ProfileServiceDelegate.class);
    }

    public static SubstitutionService getSubstitutionService() {
        return getDelegate(SubstitutionServiceDelegate.class);
    }

    public static BotService getBotService() {
        return getDelegate(BotServiceDelegate.class);
    }

    public static DefinitionService getDefinitionService() {
        return getDelegate(DefinitionServiceDelegate.class);
    }

    public static ExecutionService getExecutionService() {
        return getDelegate(ExecutionServiceDelegate.class);
    }

    public static ScriptingService getScriptingService() {
        return getDelegate(ScriptingServiceDelegate.class);
    }

    public static SynchronizationService getSynchronizationService() {
        return getDelegate(SynchronizationServiceDelegate.class);
    }

    public static TaskService getTaskService() {
        return getDelegate(TaskServiceDelegate.class);
    }

    public static ReportService getReportService() {
        return getDelegate(ReportServiceDelegate.class);
    }

    public static DataSourceService getDataSourceService() {
        return getDelegate(DataSourceServiceDelegate.class);
    }

}
