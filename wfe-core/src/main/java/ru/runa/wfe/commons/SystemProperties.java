package ru.runa.wfe.commons;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.execution.logic.ProcessExecutionListener;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;

@CommonsLog
public class SystemProperties {
    public static final String CONFIG_FILE_NAME = "system.properties";
    private static final PropertyResources RESOURCES = new PropertyResources(CONFIG_FILE_NAME);
    private static final PropertyResources NO_DATABASE_RESOURCES = new PropertyResources(CONFIG_FILE_NAME, true, false);
    private static final boolean developmentMode = "true".equals(System.getProperty("devmode"));
    private static final boolean v3CompatibilityMode = "true".equals(System.getProperty("v3compatibility"));
    public static final String WEB_SERVICE_NAMESPACE = "http://runa.ru/wfe";

    public static final String RESOURCE_EXTENSION_PREFIX = "wfe.custom.";
    public static final String DEPRECATED_PREFIX = "deprecated.";

    private static volatile List<ProcessExecutionListener> processExecutionListeners = null;

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
     * List variable back compatibility mode with version 4.2.x.
     */
    public static boolean isV4MapVariableCompatibilityMode() {
        return RESOURCES.getBooleanProperty("v4.2.map.variable.compatibility", true);
    }

    /**
     * MultiSubprocess pre 4.3.0 compatibility.
     */
    public static boolean isMultiSubprocessDataCompatibilityMode() {
        return RESOURCES.getBooleanProperty("v4.2.multi.subprocess.data.compatibility", true);
    }

    /**
     * Using cache state machine with caches isolation between transactions.
     */
    public static boolean useIsolatedCacheStateMachine() {
        return NO_DATABASE_RESOURCES.getBooleanProperty("isolated.cache.state.machine", true);
    }

    /**
     * Using stableable (formerly called "non-runtime") substitution cache instead of static substitution cache.
     */
    public static boolean useStaleableSubstitutionCache() {
        // TODO Rename parameter from "nonruntime..." to "staleable..." in configs.
        return NO_DATABASE_RESOURCES.getBooleanProperty(
                "staleable.susbstitution.cache",
                useNonRuntimeSubstitutionCache()  // fallback to old parameter name
        );
    }

    /**
     * @deprecated Queries old parameter name; use useStaleableSubstitutionCache().
     */
    @Deprecated
    private static boolean useNonRuntimeSubstitutionCache() {
        String s = NO_DATABASE_RESOURCES.getStringProperty("nonruntime.susbstitution.cache", null);
        if (s == null) {
            return true;  // default value
        }
        log.warn("Please rename obsolete config property \"nonruntime.susbstitution.cache\" to new \"staleable.susbstitution.cache\".");
        return Boolean.parseBoolean(s);  // as getBooleanProperty() does
    }

    /**
     * System version
     */
    public static String getVersion() {
        return RESOURCES.getStringProperty("version");
    }

    /**
     * System build date
     */
    public static String getBuildDateString() {
        return RESOURCES.getStringProperty("build.date");
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
        return RESOURCES.getStringProperty("file.variable.local.storage.path", IoCommons.getAppServerDirPath() + "/wfe.filedata");
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

    public static String getEARFileName() {
        return RESOURCES.getStringProperty("ear.filename", "runawfe.ear");
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

    public static boolean isDefinitionDeploymentWithCommentsCollisionsAllowed() {
        return RESOURCES.getBooleanProperty("definition.comments.collisions.allowed", false);
    }

    public static boolean isDefinitionDeploymentWithEmptyCommentsAllowed() {
        return RESOURCES.getBooleanProperty("definition.comments.empty.allowed", true);
    }

    public static boolean isCheckProcessStartPermissions() {
        return RESOURCES.getBooleanProperty("check.process.start.permissions", true);
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

    public static List<String> getProcessArchiverStepHandlers() {
        return RESOURCES.getMultipleStringProperty("process.archiver.step.handlers");
    }

    public static List<String> getProcessAdminGroupNames() {
        return RESOURCES.getMultipleStringProperty("process.admin.groups");
    }

    public static String getBaseProcessIdVariableName() {
        return RESOURCES.getStringProperty("base.process.id.variable.name");
    }

    public static boolean isBaseProcessIdModeReadAllVariables() {
        return RESOURCES.getBooleanProperty("base.process.id.variable.read.all", true);
    }

    /**
     * Max.number of integer IDs in "in (...)" clause in queries.
     */
    public static int getDatabaseParametersCount() {
        return RESOURCES.getIntegerProperty("database.parameters.count", 900);
    }

    /**
     * Max.number of string names (executor names, definition names, etc.) in "in (...)" clause in queries.
     */
    public static int getDatabaseNameParametersCount() {
        return RESOURCES.getIntegerProperty("database.name.parameters.count", 50);
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

    public static List<ProcessExecutionListener> getProcessExecutionListeners() {
        if (processExecutionListeners == null) {
            synchronized (SystemProperties.class) {
                if (processExecutionListeners == null) {
                    processExecutionListeners = Lists.newArrayList();
                    for (String className : RESOURCES.getMultipleStringProperty("process.execution.listeners")) {
                        try {
                            ProcessExecutionListener listener = ClassLoaderUtil.instantiate(className);
                            processExecutionListeners.add(listener);
                        } catch (Throwable th) {
                            processExecutionListeners = null;
                            Throwables.propagate(th);
                        }
                    }
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

    public static boolean isSwimlaneAutoInitializationEnabled() {
        return RESOURCES.getBooleanProperty("process.swimlane.auto.initialization.enabled", false);
    }

    public static boolean isProcessExecutionMessagePredefinedSelectorEnabled() {
        return RESOURCES.getBooleanProperty("process.execution.message.predefined.selector.enabled", true);
    }

    public static boolean isProcessExecutionMessagePredefinedSelectorOnlyStrictComplianceHandling() {
        return RESOURCES.getBooleanProperty("process.execution.message.predefined.selector.only.strict.compliance.handling", false);
    }

    /**
     * -1 means "disable archiving", see {@link #isProcessArchivingEnabled()}. Defaults to -1.
     */
    public static int getProcessDefaultSecondsBeforeArchiving() {
        return RESOURCES.getIntegerProperty("process.default.seconds.before.archiving", -1);
//        return RESOURCES.getIntegerProperty("process.default.seconds.before.archiving", 60);
//        return RESOURCES.getIntegerProperty("process.default.seconds.before.archiving", 365 * 86400);
    }

    public static boolean isProcessArchivingEnabled() {
        return getProcessDefaultSecondsBeforeArchiving() >= 0;
    }

    /**
     * @return default permissions by object type
     */
    public static List<Permission> getDefaultPermissions(SecuredObjectType securedObjectType) {
        List<Permission> result = new ArrayList<>();
        List<Permission> applicablePermissions = ApplicablePermissions.listVisible(securedObjectType);
        List<String> permissionNames = RESOURCES.getMultipleStringProperty(securedObjectType.toString().toLowerCase() + ".default.permissions");
        for (String permissionName : permissionNames) {
            Permission foundPermission = null;
            for (Permission permission : applicablePermissions) {
                if (permission.getName().equals(permissionName)) {
                    foundPermission = permission;
                    break;
                }
            }
            Preconditions.checkArgument(foundPermission != null, permissionName);
            result.add(foundPermission);
        }
        return result;
    }

    public static boolean isVariablesInvalidDefaultValuesAllowed() {
        return RESOURCES.getBooleanProperty("variables.invalid.default.values.allowed", false);
    }

    public static Date getVariablesInvalidDefaultValuesAllowedBefore() {
        return RESOURCES.getDateProperty("variables.invalid.default.values.allowed.before", new Date());
    }
}