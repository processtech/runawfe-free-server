package ru.runa.wfe.commons;

import java.util.Calendar;
import java.util.List;

import ru.runa.wfe.execution.logic.IProcessExecutionListener;
import ru.runa.wfe.lang.NodeType;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class SystemProperties {
    public static final String CONFIG_FILE_NAME = "system.properties";
    private static final PropertyResources RESOURCES = new PropertyResources(CONFIG_FILE_NAME);
    private static final PropertyResources NO_DATABASE_RESOURCES = new PropertyResources(CONFIG_FILE_NAME, true, false);
    private static final boolean developmentMode = "true".equals(System.getProperty("devmode"));
    private static final boolean v3CompatibilityMode = "true".equals(System.getProperty("v3compatibility"));
    public static final String WEB_SERVICE_NAMESPACE = "http://runa.ru/wfe";

    public static final String RESOURCE_EXTENSION_PREFIX = "wfe.custom.";
    public static final String DEPRECATED_PREFIX = "deprecated.";
    public static final Calendar SYSTEM_STARTUP_CALENDAR = Calendar.getInstance();

    public static final String TIMERTASK_START_MILLIS_JOB_EXECUTION_NAME = "timertask.start.millis.job.execution";
    public static final String TIMERTASK_PERIOD_MILLIS_JOB_EXECUTION_NAME = "timertask.period.millis.job.execution";
    public static final String TIMERTASK_START_MILLIS_UNASSIGNED_TASKS_EXECUTION_NAME = "timertask.start.unassigned.tasks.execution";
    public static final String TIMERTASK_PERIOD_MILLIS_UNASSIGNED_TASKS_EXECUTION_NAME = "timertask.period.millis.unassigned.tasks.execution";
    public static final String TIMERTASK_START_MILLIS_LDAP_SYNC_NAME = "timertask.start.millis.ldap.sync";
    public static final String TIMERTASK_PERIOD_MILLIS_LDAP_SYNC_NAME = "timertask.period.millis.ldap.sync";
    private static List<IProcessExecutionListener> processExecutionListeners = null;

    public static PropertyResources getResources() {
        return RESOURCES;
    }

    /**
     * Production or development mode?
     */
    public static boolean isDevMode() {
        return developmentMode;
    }

    /**
     * Process-level compatibility with version 3.x.
     */
    public static boolean isV3CompatibilityMode() {
        return v3CompatibilityMode;
    }

    /**
     * List variable back compatibility mode with version 4.2.x.
     */
    public static boolean isV4ListVariableCompatibilityMode() {
        return RESOURCES.getBooleanProperty("v4.2.list.variable.compatibility", true);
    }

    /**
     * Using cache state machine or old cache implementation.
     */
    public static boolean useCacheStateMachine() {
        return NO_DATABASE_RESOURCES.getBooleanProperty("use.cache.state.machine", true);
    }

    /**
     * Using cache state machine with caches isolation between transactions.
     */
    public static boolean useIsolatedCacheStateMachine() {
        return NO_DATABASE_RESOURCES.getBooleanProperty("isolated.cache.state.machine", true);
    }

    /**
     * System version
     */
    public static String getVersion() {
        return RESOURCES.getStringProperty("version");
    }

    public static String getStartup() {
        return CalendarUtil.formatDateTime(SYSTEM_STARTUP_CALENDAR);
    }

    public static String getAdministratorName() {
        return RESOURCES.getStringPropertyNotNull("default.administrator.name");
    }

    public static String getAdministratorDefaultPassword() {
        return RESOURCES.getStringPropertyNotNull("default.administrator.password");
    }

    public static String getAdministratorsGroupName() {
        return RESOURCES.getStringPropertyNotNull("default.administrators.group.name");
    }

    public static String getBotsGroupName() {
        return RESOURCES.getStringPropertyNotNull("default.bots.group.name");
    }

    public static String getDateFormatPattern() {
        return RESOURCES.getStringPropertyNotNull("date.format.pattern");
    }

    public static boolean isLocalFileStorageEnabled() {
        return RESOURCES.getBooleanProperty("file.variable.local.storage.enabled", true);
    }

    public static String getLocalFileStoragePath() {
        return RESOURCES.getStringProperty("file.variable.local.storage.path", IOCommons.getAppServerDirPath() + "/wfe.filedata");
    }

    public static int getLocalFileStorageFileLimit() {
        return RESOURCES.getIntegerProperty("file.variable.local.storage.enableforfilesgreaterthan", 100000);
    }

    public static String getStrongPasswordsRegexp() {
        return RESOURCES.getStringProperty("strong.passwords.regexp");
    }

    public static String getDefaultTaskDeadline() {
        return RESOURCES.getStringProperty("task.default.deadline");
    }

    /**
     * @return value between 0..100 [%]
     */
    public static int getTaskAlmostDeadlineInPercents() {
        int percents = RESOURCES.getIntegerProperty("task.almostDeadlinePercents", 90);
        if (percents < 0 || percents > 100) {
            percents = 90;
        }
        return percents;
    }

    /**
     * Change this value sync with DB.
     *
     * @return max string value
     */
    public static int getStringVariableValueLength() {
        return RESOURCES.getIntegerProperty("string.variable.length", 1024);
    }

    /**
     * ORA-24816: Expanded non LONG bind data supplied after actual LONG or LOB column (if string length > 1000)
     */
    public static int getLogMaxAttributeValueLength() {
        return RESOURCES.getIntegerProperty("log.attribute.max.length", 512);
    }

    public static int getTokenMaximumDepth() {
        return RESOURCES.getIntegerProperty("token.maximum.depth", 100);
    }

    public static boolean isLDAPSynchronizationEnabled() {
        return RESOURCES.getBooleanProperty("ldap.synchronizer.enabled", false);
    }

    public static String getEARFileName() {
        return RESOURCES.getStringProperty("ear.filename", "runawfe.ear");
    }

    public static boolean isAllowedNotDefinedVariables() {
        return RESOURCES.getBooleanProperty("undefined.variables.allowed", false);
    }

    public static boolean isStrongVariableFormatEnabled() {
        return RESOURCES.getBooleanProperty("strong.variables.format.enabled", true);
    }

    public static boolean isVariableAutoCastingEnabled() {
        return RESOURCES.getBooleanProperty("variables.autocast.enabled", true);
    }

    public static boolean isEscalationEnabled() {
        return RESOURCES.getBooleanProperty("escalation.enabled", true);
    }

    public static String getEscalationDefaultHierarchyLoader() {
        return RESOURCES.getStringProperty("escalation.default.hierarchy.loader");
    }

    public static boolean isTrustedAuthenticationEnabled() {
        return RESOURCES.getBooleanProperty("trusted.authentication.enabled", false);
    }

    public static boolean isTaskAssignmentStrictRulesEnabled() {
        return RESOURCES.getBooleanProperty("task.assignment.strict.rules.enabled", true);
    }

    public static boolean isAutoInvocationLocalBotStationEnabled() {
        return RESOURCES.getBooleanProperty("auto.invocation.local.botstation.enabled", true);
    }

    public static boolean isUpdateProcessVariablesInAPIEnabled() {
        return RESOURCES.getBooleanProperty("executionServiceAPI.updateVariables.enabled", false);
    }

    public static boolean isExecuteGroovyScriptInAPIEnabled() {
        return RESOURCES.getBooleanProperty("scriptingServiceAPI.executeGroovyScript.enabled", false);
    }

    public static boolean isUpgradeProcessToDefinitionVersionEnabled() {
        return RESOURCES.getBooleanProperty("upgrade.process.to.definition.version.enabled", true);
    }

    public static boolean isErrorEmailNotificationEnabled() {
        return getErrorEmailNotificationConfiguration() != null;
    }

    public static String getErrorEmailNotificationConfiguration() {
        return RESOURCES.getStringProperty("error.email.notification.configuration");
    }

    public static boolean isFormulaHandlerInStrictMode() {
        return RESOURCES.getBooleanProperty("formula.handler.strict.mode", false);
    }

    public static boolean isEmailGuaranteedDeliveryEnabled() {
        return RESOURCES.getBooleanProperty("email.guaranteed.delivery.enabled", false);
    }

    public static long getEmailDefaultTimeoutInMilliseconds() {
        return RESOURCES.getLongProperty("email.default.timeout.milliseconds", 10000);
    }

    public static List<String> getProcessEndHandlers() {
        return RESOURCES.getMultipleStringProperty("process.end.handlers");
    }

    public static List<String> getProcessAdminGroupNames() {
        return RESOURCES.getMultipleStringProperty("process.admin.groups");
    }

    public static String getBaseProcessIdVariableName() {
        return RESOURCES.getStringProperty("base.process.id.variable.name");
    }

    public static String getBaseProcessIdMappingVariablePrefix() {
        return RESOURCES.getStringProperty("base.process.id.mapping.variable.prefix");
    }

    public static int getDatabaseParametersCount() {
        return RESOURCES.getIntegerProperty("database.parameters.count", 900);
    }

    public static List<String> getFreemarkerStaticClassNames() {
        return RESOURCES.getMultipleStringProperty("freemarker.static.class.names");
    }

    public static boolean setPermissionsToTemporaryGroups() {
        return RESOURCES.getBooleanProperty("temporary.groups.set.permissions", false);
    }

    public static boolean deleteTemporaryGroupsOnProcessEnd() {
        return RESOURCES.getBooleanProperty("temporary.groups.delete.on.process.end", false);
    }

    public static boolean deleteTemporaryGroupsOnTaskEnd() {
        return RESOURCES.getBooleanProperty("temporary.groups.delete.on.task.end", false);
    }

    public static List<IProcessExecutionListener> getProcessExecutionListeners() {
        if (processExecutionListeners == null) {
            processExecutionListeners = Lists.newArrayList();
            for (String className : RESOURCES.getMultipleStringProperty("process.execution.listeners")) {
                try {
                    IProcessExecutionListener listener = ClassLoaderUtil.instantiate(className);
                    processExecutionListeners.add(listener);
                } catch (Throwable th) {
                    processExecutionListeners = null;
                    Throwables.propagate(th);
                }
            }
        }
        return processExecutionListeners;
    }

    public static List<String> getRequiredValidatorNames() {
        return RESOURCES.getMultipleStringProperty("required.validator.names");
    }

    public static boolean isProcessExecutionNodeAsyncEnabled(NodeType nodeType) {
        String propertyValue = RESOURCES.getStringProperty("process.execution.node.async." + nodeType);
        if (propertyValue != null) {
            return Boolean.parseBoolean(propertyValue);
        }
        return RESOURCES.getBooleanProperty("process.execution.node.async.default", false);
    }

    public static boolean isProcessSuspensionEnabled() {
        return RESOURCES.getBooleanProperty("process.suspension.enabled", true);
    }

}
